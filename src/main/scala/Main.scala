
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.util.EntityUtils
import org.apache.spark.sql.SparkSession

import java.io.IOException

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("oknbox")
      .master("local[1]")
      .getOrCreate()

    readDfAndProcessRequests(spark)
    //    def ExecuteHttpPost(url: String) : Option[String] = {


    import spark.implicits._

    def sendPostRequest(url: String, data: String): Option[String] = {
      val httpClient: CloseableHttpClient = HttpClients.createDefault()
      try {
        val httpPost = new HttpPost(url)
        httpPost.addHeader("Content-Type", "application/json")
        val quotedData = s""""$data""""  // Добавляем кавычки вокруг данных
        val entity = new StringEntity(quotedData)
        httpPost.setEntity(entity)

        val response = httpClient.execute(httpPost)
        if (response.getStatusLine().getStatusCode == 200 && response.getEntity != null) {
          Some(EntityUtils.toString(response.getEntity()))
          //val respbody = EntityUtils.toString(response.getEntity())
        } else {
          None
        }
      } catch {
        case e: IOException => e.printStackTrace(); None
      } finally {
        httpClient.close()
      }
    }


    // Основная функция для чтения датафрейма, отправки строк данных и сохранения ответов
    def readDfAndProcessRequests(sparkSession: SparkSession): Unit = {
      import sparkSession.implicits._
      val jdbcOknUrl = "jdbc:postgresql://postgresnode:5432/template1?user=postgres&password=postgres"
      val readData = spark.read.format("jdbc")
        .option("url", jdbcOknUrl)
        .option("driver", "org.postgresql.Driver")
        .option("query", "select object, fulladress from okn where fulladress is not NULL")
        .load()

      readData.show()

      val df = readData.select("fulladress")
      df.show()
      val url = "http://matchbox:8080/matchbox"

      try {
        // Преобразовать датафрейм в RDD
        val rowsRDD = df.rdd.map(row => (row.getAs[String]("fulladress")))

        // Применить функцию отправки запроса ко всем данным
        val responsesRDD = rowsRDD.mapPartitions { iterator =>
          iterator.flatMap { case (data) =>
            sendPostRequest(url, data).map(response => (response))
          }
        }//.filter(_.nonEmpty)

        // Преобразовать RDD обратно в датафрейм
        val responsesDF = responsesRDD.toDF("response")
        responsesDF.show()
      
        responsesDF.write.format("json").mode("overwrite").save("/home/javauser/output_for_matchbox")

        // Сохраняем датафрейм с результатами
        //        responsesDF.write.format("json").save(outputPath)
        //      } catch {
        //        case e: Exception => e.printStackTrace()
        //      } finally {
        //        httpClient.close()
        //      }
      }

    }
    spark.stop()

  }}
