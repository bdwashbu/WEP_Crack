package com.bdwashbu.cat.model

import java.io.File
import java.io.ByteArrayInputStream
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.DataInputStream
import java.util.ArrayList
import java.io.EOFException
import scala.collection.mutable.ListBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.bdwashbu.cat.model.IEEE_80211.FourBytes
import com.bdwashbu.cat.crack.fms.FMS
import com.bdwashbu.cat.crack.fms.IV

object CAPLoader {

  def decodeWifi(packet: PacketReader): Dissector = {
    import IEEE_80211.PacketType._
    import IEEE_80211.DataPacketSubtype

    val packetData = packet.data.toArray
    val byteStream = new ByteArrayInputStream(packetData)
    val dataStream = new DataInputStream(byteStream)

    val frameControl = {
      import IEEE_80211.PacketType
      
      val data = Endian.littleToBig(dataStream.readShort())
      val flagField = ((data & 0xFF00) >> 8).toByte.toBinaryString
      val padded = (Array.fill(8 - flagField.size % 8)('0').mkString + flagField)
      val flags = padded.map(bit => if (bit == '1') true else false)
      val frameType = PacketType((data & 0x000C) >> 2)

      IEEE_80211.FrameControl(
        version = (data & 0x0003),
        frameType = frameType,
        subType = frameType match {
          case Management => throw new Exception("802.11 MGMT packets not yet supported.")
          case Control    => throw new Exception("802.11 Control packets not yet supported.") 
          case Data       => DataPacketSubtype((data & 0x00F0) >> 4)
          case Reserved   => println("Reserved packet found"); null
        },
        toDS          = flags(7),
        fromDS        = flags(6),
        moreFragments = flags(5),
        retry         = flags(4),
        power         = flags(3),
        moreData      = flags(2),
        encrypted     = flags(1),
        order         = flags(0))
    }

    def getSeqControl = {
      val seqData = Endian.littleToBig(dataStream.readShort())
      IEEE_80211.SequenceControl(seqNumber = ((seqData & 0xFFF0) >> 4).toShort,
          						 fragmentNumber = (seqData & 0x000F).toByte)
    }

    def getQoSControl = {
      val qosData = Endian.littleToBig(dataStream.readShort())
      IEEE_80211.QualityOfServiceControl(tid       = ((qosData & 0xD000) >> 12).toByte,
          						 		 priority  = if (((qosData & 0x1000) >> 12) == 1) true else false,
          						 		 ackPolicy = ((qosData & 0x0C00) >> 8).toByte,
          						 		 msdu      = if (((qosData & 0x0200) >> 8) == 1) true else false,
          						 		 txOp      = (qosData & 0x00FF))
    }
    
    import IEEE_80211.DataPacketSubtype._
    
    val duration = Endian.littleToBig(dataStream.readShort())
    val addr1    = IEEE_80211.MACAddress((1 to 6).map(x => dataStream.readByte))
    val FCSdata  = ByteBuffer.wrap(packetData.takeRight(4)).getInt()
    
    val mac = frameControl.subType match {
      case QoSData =>
        
        val src = IEEE_80211.MACAddress((1 to 6).map(x => dataStream.readByte))
        val dst = IEEE_80211.MACAddress((1 to 6).map(x => dataStream.readByte))
        val seqControl = getSeqControl
        val qosControl = getQoSControl
        val wepData = dataStream.readInt
        
        val payload = new Array[Byte](dataStream.available() - 4)
        dataStream.read(payload)
        
        new IEEE_80211.QoSMAC(
	      frameControl = frameControl,
	      duration     = duration,
	      BSS          = addr1,
	      source       = src,
	      destination  = dst,
	      seqControl   = seqControl,
	      QoSControl   = qosControl,
	      WEP          = IEEE_80211.WEPHeader(new IV((wepData & 0xFFFFFF00) >> 8, payload(0)), (wepData & 0x000000FF).toByte),
	      payload      = payload,
	      FCS = new FourBytes(FCSdata)
	    )
	    
      case QoSNull =>
        IEEE_80211.QoSNullMAC(
	      frameControl = frameControl,
	      duration     = duration,
	      BSS          = addr1,
	      source       = IEEE_80211.MACAddress((1 to 6).map(x => dataStream.readByte)),
	      destination  = IEEE_80211.MACAddress((1 to 6).map(x => dataStream.readByte)),
	      seqControl   = getSeqControl,
	      QoSControl   = getQoSControl
	    )
      case Null =>
        IEEE_80211.NullMAC(
	      frameControl = frameControl,
	      duration     = duration,
	      BSS          = addr1,
	      source       = IEEE_80211.MACAddress((1 to 6).map(x => dataStream.readByte)),
	      destination  = IEEE_80211.MACAddress((1 to 6).map(x => dataStream.readByte))
	    )
	    
      case _ =>
        println("Unsupported 802.11 packet format: " + frameControl.subType)
        null
    } 

    IEEE_80211.Wifi(mac, packetData)
  }

  def valueOf(buf: Array[Byte]): String = buf.map("%02X" format _).mkString

  def load(file: File): Array[Packet] = {

    val isCapFile = file.getName().endsWith(".cap")
    val isNcfFile = file.getName().endsWith(".ncf")
    if (!isCapFile && !isNcfFile) {
      println("Only supports .cap and .ncf files")
    }
    
    val buffer = new ListBuffer[Packet]()

    try {

      val fChannel = new FileInputStream(file).getChannel();
      val barray: Array[Byte] = new Array(file.length().asInstanceOf[Int])
      val bb = ByteBuffer.wrap(barray);
      bb.order(ByteOrder.LITTLE_ENDIAN);
      fChannel.read(bb);

      val inputStream = new DataInputStream(new ByteArrayInputStream(barray))

      // just throw away the file header for now, if there is one
      if (isCapFile) Cap.readFileHeader(inputStream) else Ncf.readFileHeader(inputStream)

      var index = 0

      while (true) {

        val capPacket = if (isCapFile) Cap.readPacketHeader(inputStream) else Ncf.readPacketHeader(inputStream)

        buffer += new Packet(capPacket, index) {
          lazy val data = decodeWifi(capPacket)
        }

        index = index + 1
      }

      return Array()

    } catch {
      case e: EOFException => null
      case e: Exception => e.printStackTrace()
      case e: EOFException => println("End of file reached")
    }

    return buffer.toArray
  }

}