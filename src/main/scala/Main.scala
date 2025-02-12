
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
    //
    //      val client: OkHttpClient = new OkHttpClient();
    //      val headerBuilder = new Headers.Builder
    //      val headers = headerBuilder
    //          .add("content-type", "application/json")
    //          .build
    //
    //      val formBody = new FormBody.Builder()
    //
    //      val body = formBody
    //          .add("objectnum","1234567")
    //          .build
    //
    //      val result = try {
    //        val request = new Request.Builder()
    //          .url(url)
    //          .headers(headers)
    //          .post(body)
    //          .build();
    //
    //        val response: Response = client.newCall(request).execute()
    //        response.body().string()
    //      }
    //      catch {
    //        case _: Throwable => null
    //      }
    //      Option[String](result)
    //    }
    //
    //    val restApiSchema = StructType(List(
    //      StructField("objectnum", StringType, true),
    //      StructField("fiasdata", StringType, true)
    //    ))
    //
    //    val executeRestApiUDF = udf(new UDF1[String, String] {
    //      override def call(url: String) = {
    //        ExecuteHttpPost(url).getOrElse("")
    //      }
    //    }, StringType)
    //
    //    case class RestAPIRequest (url: String)
    //
    //    val restApiCallsToMake = RestAPIRequest("217.197.116.167:8080/matchbox")
    //    val source_df = restApiCallsToMake.toDF()
    //
    //    val execute_df = source_df
    //      .withColumn("result", executeRestApiUDF(col("url")))
    //      .withColumn("result", from_json(col("result"), restApiSchema))
    //
    //    execute_df.select(explode(col("result.Results")).alias("makes"))
    //      .select(col("makes.Make_ID"), col("makes.Make_Name"))
    //      .show

    import spark.implicits._

    // Define the URL and JSON payload for the POST request
    //    val url = "https://jsonplaceholder.typicode.com/posts"
    //    val jsonPayload = """{"objectnum","1234567"}"""
    //
    //    val client: OkHttpClient = new OkHttpClient();
    //    val requestBody = jsonPayload.toRequestBody(mediaType)
    //    val headerBuilder = new Headers.Builder
    //    val headers = headerBuilder
    //      .add("content-type", "application/json")
    //      .build
    //
    //    val result = try {
    //      val request = new Request.Builder()
    //        .url(url)
    //        .headers(headers)
    //        .post(requestBody)
    //        .build();
    //
    //      val response: Response = client.newCall(request).execute()
    //      response.body().string()
    //    }
    //    catch {
    //      case _: Throwable => "Something went wrong"
    //    }
    //
    //    print(result)

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

      //val jdbcOknUrl = "jdbc:postgresql://localhost:5432/template1?user=postgres&password=postgres"
      val jdbcOknUrl = "jdbc:postgresql://postgresnode:5432/template1?user=postgres&password=postgres"
      val readData = spark.read.format("jdbc")
        .option("url", jdbcOknUrl)
        .option("driver", "org.postgresql.Driver")
        .option("query", "select object, fulladress from okn where fulladress is not NULL")
        .load()

      readData.show()

      val df = readData.select("fulladress")
      df.show()
      //val url = "http://localhost:9099/matchbox"
      //val url = "http://217.197.116.167:8080/matchbox"
      val url = "http://matchbox:8080/matchbox"

      try {
        // Преобразовать датафрейм в RDD
        val rowsRDD = df.rdd.map(row => (row.getAs[String]("fulladress")))
        //println(rowsRDD)
        // Применить функцию отправки запроса ко всем данным
        val responsesRDD = rowsRDD.mapPartitions { iterator =>
          iterator.flatMap { case (data) =>
            sendPostRequest(url, data).map(response => (response))
          }
        }//.filter(_.nonEmpty)

        // Преобразовать RDD обратно в датафрейм
        val responsesDF = responsesRDD.toDF("response")
        responsesDF.show()
        //responsesDF.write.format("json").mode("overwrite").save("/home/dmitry/temp/okn/output_for_matchbox")
        responsesDF.write.format("json").mode("overwrite").save("/home/temp/okn/output_for_matchbox")

        // Сохраняем датафрейм с результатами
        //        responsesDF.write.format("json").save(outputPath)
        //      } catch {
        //        case e: Exception => e.printStackTrace()
        //      } finally {
        //        httpClient.close()
        //      }
      }

    }
    //-----------------------------------------------------------------------------------------
    //    val httpClient = HttpClients.createDefault()
    //    val postRequest = new HttpPost("http://217.197.116.167:8080/matchbox")
    //    postRequest.setHeader("Content-Type", "application/json")
    //
    //    val data = """"1234567""""
    //    println(data)
    //
    //    postRequest.setHeader("Content-Type", "application/json")
    //    postRequest.setEntity(new StringEntity(data))
    //    // Если у вас есть параметры запроса, вы можете добавить их здесь
    //
    //    val response = httpClient.execute(postRequest)
    //    val code = response.getStatusLine().getStatusCode() // Получить статус запроса
    //    //val respbody  = response.getEntity().getContent() // Получить тело ответа
    //    val entity = response.getEntity()
    //    val respbody = EntityUtils.toString(entity)
    //    println(code)
    //    println(respbody)
    //    httpClient.close()
    //------------------------------------------------------------------------------------
    spark.stop()

  }}
