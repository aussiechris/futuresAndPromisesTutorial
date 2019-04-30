package cosc250.weekSix

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.io.Source.fromURL

object Exercise {


  /*
   * First, let's just do some basic things with Promise and Future
   */


  /**
    * Just complete this promise with a number. Have a look at what the test is doing
    */
  def completeMyPromise(p: Promise[Int]): Unit = p.success(1)

  /**
    * I'm going to give you a Future[Int]. Double it and return it.
    * Have a look at what the test is doing
    */
  def doubleMyFuture(p: Future[Int]): Future[Int] = p.map(_ * 2)

  /**
    * Let's chain a few things together.
    * I'm going to give you two Future[String]s. You're going to convert them both to uppercase, and count how many
    * letters are identical in each
    *
    * Hint: use for { a <- fut } notation
    *
    * Don't use isComplete.
    */
  def compareMyFutureStrings(fs1: Future[String], fs2: Future[String]): Future[Int] = {
    // for each string in fs1 and fs2
    for {
      st1 <- fs1
      st2 <- fs2
    } yield {
      // compare characters at each position and return the number of matching pairs
      (for {p1 <- 0 until st1.length
            if st1.toUpperCase.charAt(p1) == st2.toUpperCase.charAt(p1)}
        yield 1).sum
    }
  }

  /**
    * Here's an example of parsing a JSON string
    */
  def nameFromJason() = {
    val json2: JsValue = Json.parse(
      """
      {
        "name" : "Watership Down",
        "location" : {
          "lat" : 51.235685,
          "long" : -1.309197
        },
        "residents" : [ {
          "name" : "Fiver",
          "age" : 4,
          "role" : null
        }, {
          "name" : "Bigwig",
          "age" : 6,
          "role" : "Owsla"
        } ]
      }
     """)

    val name = (json2 \ "name").as[String]

    name
  }


  /*
   * This stuff sets up our web client
   */
  implicit val system = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer()
  val wsClient: AhcWSClient = AhcWSClient()


  /**
    * Here's an example of using the Web Client.
    */
  def webExample() = {
    wsClient
      .url("http://turing.une.edu.au/~cosc250/lectures/cosc250/test.txt")
      .get()
      .map(_.body)
  }


  /**
    * Your first challenge...
    *
    * Get the file http://turing.une.edu.au/~cosc250/lectures/cosc250/second.json and extract the name from the JSON
    */
  def secondName(): Future[String] = {
    // get the json file from a url
    val unparsedJson: Future[String] = Future(fromURL("http://turing.une.edu.au/~cosc250/lectures/cosc250/second.json").mkString)

    // parse the json file
    val json: Future[JsValue] = for {j <- unparsedJson} yield {
      Json.parse(j)
    }

    // return the name
    for {j <- json} yield {
      (j \ "name").as[String]
    }
  }

  /**
    * Your second challenge...
    *
    * Get the file from url1
    * Get the file from url2
    * Parse them each as JSON
    * and case insensitively see how many characters are in common in the two names...
    */
  def nameCharactersInCommon(url1: String, url2: String): Future[Int] = {
    // fetch and parse the json files from the supplied urls
    val json1 = Future(Json.parse(fromURL(url1).mkString))
    val json2 = Future(Json.parse(fromURL(url2).mkString))

    // get the names from each json file
    val name1 = for {j1<-json1} yield {(j1 \ "name").as[String]}
    val name2 = for {j2<-json2} yield {(j2 \ "name").as[String]}

    // compare the names and return the result
    compareMyFutureStrings(name1, name2)
  }
}
