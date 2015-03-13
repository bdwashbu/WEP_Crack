package com.bdwashbu.cat.reflection
import reflect.runtime.universe
import reflect.runtime.universe._
import scala.reflect.runtime.JavaMirrors

object Reflection {

  val mirror = universe.runtimeMirror(getClass.getClassLoader)
  
  def getDeclaredFields(obj: Any) = {
    
      val instanceMirror = mirror.reflect(obj)
      val typeSig = instanceMirror.symbol.asType.typeSignature
      
      val decl = typeSig.declarations.filter(x => x.isTerm && !x.isType && !x.isMethod && !x.isModule && !x.isJava).filterNot(_.name.decoded.startsWith("_")).map(_.asTerm)
	  decl.filter(field => instanceMirror.reflectField(field).get != null).map(field => instanceMirror.reflectField(field))
	}
  
  
  
  def getAllDeclaredFields(obj: Any): Array[FieldMirror] = {
    
    val results: Array[FieldMirror] = if (obj.isInstanceOf[FieldMirror]) {
	
			if (Reflection.isEnumeration(obj)) {
			  Array()
			} else {
			  val actualObject = obj.asInstanceOf[FieldMirror].get
			  if (actualObject != null) {
				  val decl = getDeclaredFields(actualObject)
					if (decl.size == 1)
					  Array()
					else {
					   decl.flatMap(x => x +: getAllDeclaredFields(x)).toArray
					}
			  } else Array()
			}
		  }
		  else {
		    val decl = getDeclaredFields(obj)
		    decl.flatMap(x => x +: getAllDeclaredFields(x)).toArray
		  }
    
      results.filter(x => x.symbol.typeSignature.typeSymbol.asClass.isPrimitive || x.symbol.typeSignature =:= universe.typeTag[Array[Byte]].tpe)
  }
  
  def isEnumeration(obj: Any): Boolean = {
    
    def getTypeSymbol(obj: Any): Symbol = {
	   obj.asInstanceOf[FieldMirror].symbol.typeSignature.typeSymbol.owner.typeSignature.typeSymbol
	}
    
    val typeSymbol = getTypeSymbol(obj.asInstanceOf[FieldMirror])
    typeSymbol.typeSignature <:< universe.typeTag[Enumeration].tpe.typeSymbol.typeSignature
  }  
}