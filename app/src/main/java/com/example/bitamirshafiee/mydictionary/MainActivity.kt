package com.example.bitamirshafiee.mydictionary

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun findWord(view:View ){

        var stringUrl = "https://od-api.oxforddictionaries.com:443/api/v1/entries/en/"+edit_text.text.toString()

        var myAsyncTask = MyAsynchTask()

        myAsyncTask.execute(stringUrl)
    }

    inner class MyAsynchTask : AsyncTask<String,Void, Data>(){
        override fun doInBackground(vararg params: String?): Data? {
            val url = createUrl(params[0])

            var jsonResponse : String?

            try {

                jsonResponse = makeHttpResponse(url)
                val data = extractFeatureFromJson(jsonResponse)
                return data
            }catch (e:IOException){
                Log.e("MainActivity","Problem Making the HTTP request :"+ e)
            }

            return null

        }

        override fun onPostExecute(result: Data?) {
            super.onPostExecute(result)

            if (result == null){
                return
            }
            showDefinition(result.definition)
        }

    }

    fun showDefinition(definition :String?){

        val intent = Intent(this, Definition::class.java)

        intent.putExtra("myDefinition",definition)

        startActivity(intent)

    }

    fun extractFeatureFromJson(definitionJson : String?):Data?{
        try {
            val baseJsonResponse = JSONObject(definitionJson)
            val featureResults = baseJsonResponse.getJSONArray("results")
            val firstResult = featureResults.getJSONObject(0)
            val lexicalEntries = firstResult.getJSONArray("lexicalEntries")
            val firstLexicalEntry = lexicalEntries.getJSONObject(0)
            val entries = firstLexicalEntry.getJSONArray("entries")
            val firstEntry = entries.getJSONObject(0)
            val senses = firstEntry.getJSONArray("senses")
            val firstSense = senses.getJSONObject(0)
            val definitions = firstSense.getJSONArray("definitions")

            Log.d("definition","it is:"+definitions[0])

            return Data(definitions[0].toString())
        }catch (e : JSONException){
            Log.e("MainActivity","Error in parsing JSON"+e)
        }
        return null
    }

    fun makeHttpResponse(url : URL?):String{

        var jsonResponse = ""
        var urlConnection : HttpURLConnection

        var inputStream : InputStream? = null

        try {

            urlConnection = url?.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.setRequestProperty("Accept", "Application/json")
            urlConnection.setRequestProperty("app_id","4b2732f2")
            urlConnection.setRequestProperty("app_key","157ae73071447a1c38b4b47fbd4cd4af")
            urlConnection.readTimeout = 10000
            urlConnection.connectTimeout = 15000
            urlConnection.connect()

            if (urlConnection.responseCode == 200){
                inputStream = urlConnection.inputStream
                jsonResponse = readFromInputStream(inputStream)
            }else{
                Log.d("MainActivity","Error Response Code: "+urlConnection.responseCode)
            }

            urlConnection.disconnect()
            inputStream?.close()
        }catch (e : IOException){
            Log.e("MainActivity","connection Error"+e)
        }


        return jsonResponse
    }

    fun readFromInputStream(inputStream : InputStream?):String{
        val output = StringBuilder()

        if (inputStream != null){
            val inputStreamReader = InputStreamReader(inputStream, Charset.forName("UTF-8"))
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()

            while (line != null){
                output.append(line)
                line = reader.readLine()
            }
        }
        return output.toString()
    }

    fun createUrl(stringUrl: String?): URL?{
        var url : URL? = null

        try {

            url = URL(stringUrl)
        }catch (exception : MalformedURLException){
            Log.d("MainActivity", "ERROR IN CREATING URL")
            return null
        }

        return url
    }
}
