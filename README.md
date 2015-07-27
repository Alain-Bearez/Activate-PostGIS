# Activate-PostGIS
This extension allows for using some PostGis functions with the Activate persistence framework.

In order to use this extension, you need to add the ```PostGisContext``` trait to your storage, along the ```ActivateContext```.  The ```dialect``` is then defined to be the ```postgisDialect``` this extension is providing.
```scala
object ApplicationStorage extends PostGisContext with ActivateContext {
  val storage = 
    new AsyncPostgreSQLStorage {
      def configuration =
        new AsyncConfiguration(
          host = Configuration.dbUrl,
          port = Configuration.dbPort,
          username = Configuration.dbUsr,
          password = Some(Configuration.dbPwd),
          database = Some(Configuration.dbName))
      lazy val objectFactory = new PostgreSQLConnectionFactory(configuration)
      override val dialect = postgisDialect
    }
  /* ... */
}
```

## Updating the Geometry field from a GeoJSON field

In your entity, you can define the change operation calling the update.
```scala
  override protected def afterInitialize = {
    on(_.jsonfence).change {
      transactional {
        val update = (this, Map(
          "id" -> StringStorageValue(Option(id)),
          "geofence" -> PostGisStorageValue(jsonfence)))
        import scala.concurrent.ExecutionContext.Implicits.global
        storage.storeAsync(Nil, Nil, Nil, List(update), Nil)
      }
    }
  }
```

Now, you can define a method to check whether some set of points are completely enclosed within the fence.
```scala
  def isStretchOutOfFence(placeId: String, stretchId: String): Boolean = transactional {
    query {
      (place: PlaceTable, stretch: StretchTable) =>
        where((place.id :== placeId) :&& (stretch.id :== stretchId) :&&
          (stretch.geopoints.isNotNull) :&& (place.geofence :|+ stretch.geopoints)) select (place, stretch)
    }.isEmpty
  }
```
