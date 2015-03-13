package com.bdwashbu.cat.model

import java.io.DataInputStream

trait Dissector

abstract class Packet(val header: PacketReader, val index: Int) {
  def data: Dissector
}

trait PacketReader {
    def data: IndexedSeq[Byte]
    def dataLength: Int
    def time: Double
  }

trait CaptureLoader {
  def readPacketHeader(stream: DataInputStream): PacketReader
  def readFileHeader(stream: DataInputStream)
}

object Ncf extends CaptureLoader {
  
  def readFileHeader(stream: DataInputStream) = {}
  
  def readPacketHeader(stream: DataInputStream) = {
    
    //val seconds = Endian.littleToBig(stream.readInt())
    //val uSeconds = Endian.littleToBig(stream.readInt())
    //val numOctets = Endian.littleToBig(stream.readInt())
    val packetLength = Endian.littleToBig(stream.readShort())
    
    val ignore = new Array[Byte](22)
    stream.read(ignore)

    val packetData = new Array[Byte](packetLength)
    stream.read(packetData)
   
    new PacketReader {
      def data = packetData
      def dataLength = packetLength
      def time = 0
    }
  }
  
}

object Cap extends CaptureLoader {
  
  case class GlobalHeader(
    val magicNumber: Int,
    val versionMajor: Short,
    val versinoMinor: Short,
    val thisZone: Int,
    val sigFigs: Int,
    val maxLength: Int,
    val network: Int
	)
	
	object GlobalHeader {
	  def read(stream: DataInputStream) = {
	    GlobalHeader(
	      stream.readInt(),
	      stream.readShort(),
	      stream.readShort(),
	      stream.readInt(),
	      stream.readInt(),
	      stream.readInt(),
	      stream.readInt()
	    ) 
	  }
	}
  
  def readFileHeader(stream: DataInputStream) = GlobalHeader.read(stream)
  
  def readPacketHeader(stream: DataInputStream) = {

    val seconds = Endian.littleToBig(stream.readInt())
    val uSeconds = Endian.littleToBig(stream.readInt())
    val numOctets = Endian.littleToBig(stream.readInt())
    val packetLength = Endian.littleToBig(stream.readInt())
    val packetData = new Array[Byte](packetLength)
    stream.read(packetData)
   
    new PacketReader {
      def data = packetData
      def dataLength = packetLength
      def time = ((seconds) + (uSeconds)/1000000.0)
    }
  }
}