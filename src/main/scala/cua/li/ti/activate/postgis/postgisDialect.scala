package cua.li.ti.activate.postgis

import scala.collection.mutable.{ Map => MutableMap }
import net.fwbrasil.activate.statement.{ CompositeOperatorCriteria, Criteria, FunctionApply, StatementValue }
import net.fwbrasil.activate.storage.marshalling.{ ListStorageValue, StorageOptionalValue, StorageValue }
import net.fwbrasil.activate.storage.relational.{ DmlStorageStatement, NormalQlStatement, StorageStatement, UpdateStorageStatement }
import net.fwbrasil.activate.storage.relational.idiom.postgresqlDialect

object postgisDialect extends postgisDialect

class postgisDialect extends postgresqlDialect(pEscape = string => "\"" + string + "\"", pNormalize = string => string) {
  override def toSqlDmlFunctionApply(value: FunctionApply[_])(implicit binds: MutableMap[StorageValue, String]): String =
    value match {
      case value: AsGeoJson        => stringAsGeoJson(toSqlDml(value.value))
      case value: FromGeoJson      => stringFromGeoJson(toSqlDml(value.value))
      case value: FunctionApply[_] => super.toSqlDmlFunctionApply(value)
    }
  def stringAsGeoJson(value: String): String = s"ST_AsGeoJSON($value)"
  def stringFromGeoJson(value: String): String = s"ST_GeomFromGeoJSON($value)"
  def stringCoversGeo(polygon: String, points: String): String = s"ST_Covers($polygon, $points)"
  override def toSqlDml(value: Criteria)(implicit binds: MutableMap[StorageValue, String]): String =
    value match {
      case CompositeOperatorCriteria(valueA: StatementValue, operator: Covers, valueB: StatementValue) =>
        stringCoversGeo(toSqlDml(valueA), toSqlDml(valueB))
      case _ => super.toSqlDml(value)
    }
  override def toSqlStatement(statement: StorageStatement): List[NormalQlStatement] =
    statement match {
      /*
      case insert: InsertStorageStatement if (hasPostGis(insert)) =>
        if (hasList(insert)) {
          throw new IllegalStateException("Cannot have PostGIS objects and lists on the same entity.")
        }
        val sortedKeys = insert.propertyMap.keys.toList.sorted
        val mainStatement = new NormalQlStatement(
          statement = "INSERT INTO " + toTableName(insert.entityClass) +
            " (" + sortedKeys.map(escape).mkString(", ") + ") " +
            " VALUES (" + sortedKeys.map(fromGeo(_, insert.propertyMap)).mkString(", ") + ")",
          entityClass = insert.entityClass,
          binds = insert.propertyMap,
          expectedNumberOfAffectedRowsOption = Some(1))
        List(mainStatement)
      */
      case update: UpdateStorageStatement if (hasPostGis(update)) =>
        if (hasList(update)) {
          throw new IllegalStateException("Cannot have PostGIS objects and lists on the same entity.")
        }
        val mainStatement = new NormalQlStatement(
          statement = "UPDATE " + toTableName(update.entityClass) +
            " SET " + (for (key <- update.propertyMap.keys.toList.sorted if (key != "id")) yield
                escape(key) + " = " + fromGeo(key, update.propertyMap)).mkString(", ") +
            " WHERE ID = :id" + versionCondition(update.propertyMap),
          entityClass = update.entityClass,
          binds = update.propertyMap,
          expectedNumberOfAffectedRowsOption = Some(1))
        List(mainStatement)
      case _ => super.toSqlStatement(statement)
    }
  private def fromGeo(key: String, properties: Map[String, StorageValue]): String = {
    // this is a marshalling function upon insert and update for the property value s"ST_GeomFromGeoJSON($value)"
    properties(key) match {
      case gis: PostGisStorageValue => s"ST_GeomFromGeoJSON(:$key)"
      case _                        => s":$key"
    }
  }
  private def hasList(statement: DmlStorageStatement): Boolean = {
    val (listPropertyMap, normalPropertyMap) = statement.propertyMap.partition(tuple => tuple._2.isInstanceOf[ListStorageValue])
    listPropertyMap.nonEmpty
  }
  private def hasPostGis(statement: DmlStorageStatement): Boolean = {
    val (postGisPropertyMap, normalPropertyMap) = statement.propertyMap.partition(tuple => tuple._2.isInstanceOf[PostGisStorageValue])
    postGisPropertyMap.nonEmpty
  }
}
case class PostGisStorageValue(override val value: Option[String])
    extends StorageOptionalValue {
  type T = String
}
