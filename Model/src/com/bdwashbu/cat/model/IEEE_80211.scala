package com.bdwashbu.cat.model

import com.bdwashbu.cat.crack.fms.FMS
import com.bdwashbu.cat.crack.fms.IV

object IEEE_80211 {

  object SNAP {
    val header = 0xAA
  }
  
  case class MACAddress(val addr: IndexedSeq[Byte]) {
    override def toString() = addr.map(x => (x.toChar & 0xFF).toHexString.padTo(2, '0')).reduce(_ + ":" + _)
  }

  object PacketType extends Enumeration {
    type PacketType = Value
    val Management = Value(0)
    val Control = Value(1)
    val Data = Value(2)
    val Reserved = Value(3)
  }

  trait PacketSubtype extends Enumeration {
    type PacketSubtype = Value
  }

  object DataPacketSubtype extends PacketSubtype {
    type DataPacketSubtype = Value
    val Data = Value(0)
    val DataPlusCFAck = Value(1)
    val DataPlusCFPoll = Value(2)
    val DataPlusCFAckPlusCFPoll = Value(3)
    val Null = Value(4)
    val CFAck = Value(5)
    val CFPoll = Value(6)
    val CFAckCFPoll = Value(7)
    val QoSData = Value(8)
    val QoSDataPlusCFAck = Value(9)
    val QoSDataPlusCFPoll = Value(10)
    val QoSDataPlusCFAckPlusCFPoll = Value(11)
    val QoSNull = Value(12)
    val Reserved = Value(13)
    val QoSPlusCFPoll = Value(14)
    val QoSPlusCFAck = Value(15)
  }

  import PacketType._
  import DataPacketSubtype._

  case class FrameControl(val version: Int,
                          val frameType: PacketType,
                          val subType: DataPacketSubtype,
                          val toDS: Boolean,
                          val fromDS: Boolean,
                          val moreFragments: Boolean,
                          val retry: Boolean,
                          val power: Boolean,
                          val moreData: Boolean,
                          val encrypted: Boolean,
                          val order: Boolean)

  case class SequenceControl(val seqNumber: Short,
                             val fragmentNumber: Byte)

  case class QualityOfServiceControl(val tid: Byte,
                                     val priority: Boolean,
                                     val ackPolicy: Int,
                                     val msdu: Boolean,
                                     val txOp: Int)

  case class WEPHeader(val InitVector: IV,
                       val ID: Byte)
                       
  class FourBytes(val value: Int) {
    override def toString() = "0x" + ((value & 0xFF000000) >> 24).toChar.toHexString +
    						         ((value & 0x00FF0000) >> 16).toChar.toHexString +
                                     ((value & 0x0000FF00) >> 8).toChar.toHexString +
                                     (value & 0x000000FF).toChar.toHexString
                                     
     def toArray: Array[Byte] = {
      Array(((value & 0xFF000000) >> 24).toByte, ((value & 0x00FF0000) >> 16).toByte, ((value & 0x0000FF00) >> 8).toByte, (value & 0x000000FF).toByte)
    }
  } 
  
  

  trait MACFrame {
    def frameControl: FrameControl
    def duration: Short
    def BSS: MACAddress
    def source: MACAddress
    def destination: MACAddress
  }
  
  trait DataFrame {
    def payload: Array[Byte]
  }

  case class QoSNullMAC(val frameControl: FrameControl,
                     val duration: Short,
                     val BSS: MACAddress,
                     val source: MACAddress,
                     val destination: MACAddress,
                     val seqControl: SequenceControl,
                     val QoSControl: QualityOfServiceControl) extends MACFrame

  case class QoSMAC(val frameControl: FrameControl,
                    val duration: Short,
                    val BSS: MACAddress,
                    val source: MACAddress,
                    val destination: MACAddress,
                    val seqControl: SequenceControl,
                    val QoSControl: QualityOfServiceControl,
                    val WEP: WEPHeader,
                    val payload: Array[Byte],
                    val FCS: FourBytes) extends MACFrame with DataFrame 
                    
  case class NullMAC(val frameControl: FrameControl,
                     val duration: Short,
                     val BSS: MACAddress,
                     val source: MACAddress,
                     val destination: MACAddress) extends MACFrame

  case class Wifi(val header: MACFrame,
                  val rawBytes: Array[Byte]) extends Dissector
}