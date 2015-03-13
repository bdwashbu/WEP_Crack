package com.bdwashbu.cat.crack

 trait StreamCipher {
    def key: Array[Byte]
    def keyStream(): Stream[Int]
    def decrypt(encrypted: Seq[Byte]): Seq[Byte]
    def encrypt(encrypted: Seq[Byte]): Seq[Byte]
  }