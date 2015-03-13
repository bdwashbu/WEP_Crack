package com.bdwashbu.cat.model

import java.io.DataInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Endian {
  def littleToBig(i: Int): Int = {
    ((i&0xff)<<24) + ((i&0xff00)<<8) + ((i&0xff0000)>>8) + ((i>>24)&0xff)
  }
  
  def littleToBig(i: Short): Short = {
    (((i & 0x00ff) << 8) + ((i & 0xff00) >> 8)).toShort
  }
}