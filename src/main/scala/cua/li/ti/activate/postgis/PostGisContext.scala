package cua.li.ti.activate.postgis

import net.fwbrasil.activate.entity.StringEntityValue
import net.fwbrasil.activate.statement.{ ComparationOperator, CompositeOperatorCriteria, FunctionApply, StatementValue, StatementSelectValue }

trait PostGisContext {
  def asGeoJson[V](value: V)(implicit tval1: (=> V) => StatementSelectValue) = AsGeoJson(value)
  def fromGeoJson[V](value: V)(implicit tval1: (=> V) => StatementSelectValue) = FromGeoJson(value)
  import scala.language.implicitConversions
  implicit def toCovers[V](value: V)(implicit tval1: (=> V) => StatementSelectValue) = Covers(value)
}
case class AsGeoJson(value: StatementSelectValue)
    extends FunctionApply(value) {
  override def toString = s"asGeoJson($value)"
  def entityValue = value.entityValue.asInstanceOf[StringEntityValue].value
}
case class FromGeoJson(value: StatementSelectValue)
    extends FunctionApply(value) {
  override def toString = s"fromGeoJson($value)"
  def entityValue = value.entityValue.asInstanceOf[StringEntityValue].value
}
case class Covers(valueA: StatementSelectValue) extends ComparationOperator {
    def :|+(valueB: StatementValue) = CompositeOperatorCriteria(valueA, this, valueB)
    override def toString = ":|+"
}
