package com.bdwashbu.cat.crack.fms

import com.bdwashbu.cat.crack.StreamCipher

object RC4 {
  def apply(IV: Array[Byte], password: IndexedSeq[Byte]): RC4Cipher = {
    new RC4Cipher(IV ++ password)
  }

  def apply(keyStream: Array[Byte]): RC4Cipher = {
    new RC4Cipher(keyStream)
  }
}

class RC4Cipher(val key: Array[Byte]) extends StreamCipher {
  var S = Array[Int]()
  var j: Int = 0

  def mod(x: Int, y: Int): Int = (x % y + y) % y
  
  def swap(x: Int, y: Int) = {
    val result = S(x)
    S(x) = S(y)
    S(y) = result
  }

  def ksa(numItr: Int) = {
    S = (0 to 255).toArray

    j = 0
    for (i <- 0 until numItr) {
      j = mod(j + S(i) + key(mod(i, key.length)), 256)
      swap(i, j)
    }
  }

  def keyStream: Stream[Int] = prga() #:: keyStream()

  object prga {
    ksa(256)
    var i: Int = 0
    var j: Int = 0

    def apply(): Int = {
      i = mod((i + 1), 256)
      j = mod((j + S(i)), 256)

      swap(i, j)

      (S(mod((S(i) + S(j)), 256)))
    }
  }

  def decrypt(encrypted: Seq[Byte]): Seq[Byte] = {
    keyStream.take(encrypted.length).zip(encrypted).map { case (x, y) => (x.toByte ^ y.toByte).toByte }.toArray
  }
  
  def decrypt(encrypted: Byte): Byte = {
    (encrypted ^ keyStream.take(1).head).toByte
  }

  def encrypt(data: Seq[Byte]): Seq[Byte] = {
    keyStream.take(data.length).zip(data).map { case (x, y) => (x.toByte ^ y.toByte).toByte }.toArray
  }
}