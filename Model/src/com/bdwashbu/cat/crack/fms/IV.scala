package com.bdwashbu.cat.crack.fms

import com.bdwashbu.cat.model.PacketReader
import com.bdwashbu.cat.model.IEEE_80211.DataFrame
import com.bdwashbu.cat.model.IEEE_80211

case class IV(val value: Int, val firstPayloadByte: Byte) {
    
    lazy val cipher = {
      val result = RC4(toArray)
      result.ksa(3)
      result
    }
    
    def getFirstKeyByte = (firstPayloadByte ^ IEEE_80211.SNAP.header) & 0x00FF
    
    override def toString() = "0x" + ((value & 0x00FF0000) >> 16).toChar.toHexString +
                                     ((value & 0x0000FF00) >> 8).toChar.toHexString +
                                     (value & 0x000000FF).toChar.toHexString
    def toArray: Array[Byte] = {
      Array(((value & 0x00FF0000) >> 16).toByte, ((value & 0x0000FF00) >> 8).toByte, (value & 0x000000FF).toByte)
    }
    
    def mod(x: Int, y: Int): Int = (x % y + y) % y
    
    def isWeak(N: Int): Boolean = cipher.S(0) + cipher.S(1) == N+3
    
    // The classic, most powerful version of 'weak' from the FMS paper: (N+3, 255, X)
    
    def isVeryWeak(N: Int): Boolean = (toArray(0) & 0x00FF) == N+3 && (toArray(1) & 0x00FF) == 0xFF
    
    def isSuperWeak(N: Int): Boolean = cipher.S(1) + cipher.S(cipher.S(1)) == 3+N && cipher.S(1) < 3
    
    def isAdvancedWeak(B: Int): Boolean = {
      
        val x = toArray(0) & 0x00FF
	    val y = toArray(1) & 0x00FF
	    val z = toArray(2) & 0x00FF
	    val a = mod(x + y, 256)
	    val b = mod((x + y) - z, 256)
	    val N = 256
      
	    if ((((0 <= a && a < B) ||
	     (a == B && b == (B + 1) * 2)) &&
	     (if (B % 2 == 0) a != ((B + 1) / 2) else true)) ||
	     (a == B + 1 && (if (B == 0) b == (B + 1) * 2 else true)) ||
	     (x == (B + 3) && y == N - 1) ||
	     (if (B != 0 && (B % 2 != 0)) (x == 1 && y == (B / 2) + 1) ||
	     (x == (B / 2) + 2 && y == (N - 1) - x) else false))
	      true else false
      
    }
  }  