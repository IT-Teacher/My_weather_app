package uz.itteacher.myweatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import coil.load
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import uz.itteacher.myweatherapp.adapter.DayAdapter
import uz.itteacher.myweatherapp.adapter.HourAdapter
import uz.itteacher.myweatherapp.databinding.ActivityMainBinding
import uz.itteacher.myweatherapp.model.Day

const val TAG = "TAG"

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var dayAdapter: DayAdapter
    lateinit var hourAdapter: HourAdapter

    //    lateinit var hourList: MutableList<Hour>
    lateinit var days: MutableList<Day>

    val url =
        "https://api.weatherapi.com/v1/forecast.json?key=9a309351c5634f61bce115653231506&q=London&days=3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        days = mutableListOf()
        dayAdapter = DayAdapter(days)

        binding.rvDay.adapter = dayAdapter

        val requestQueue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(url, object : Response.Listener<JSONObject> {
            override fun onResponse(response: JSONObject?) {
                mainInfo(response!!)

                val forecast = response.getJSONObject("forecast")

                val forecastday = forecast.getJSONArray("forecastday")
                for (i in 0 until forecastday.length()){
                    val obj = forecastday.getJSONObject(i)
                    val date = obj.getString("date")
                    val day = obj.getJSONObject("day")
                    val maxtemp_c = day.getString("maxtemp_c")
                    val mintemp_c = day.getString("mintemp_c")
                    val condition = day.getJSONObject("condition")
                    val con_text = condition.getString("text")
                    val day_icon = "http:" + condition.getString("icon")
                    days.add(Day(date,maxtemp_c,mintemp_c,con_text,day_icon))
                    dayAdapter.notifyDataSetChanged()

                }

            }

        }, object : Response.ErrorListener {
            override fun onErrorResponse(error: VolleyError?) {
                Log.d(TAG, "onErrorResponse: $error")
            }

        })
        requestQueue.add(request)

    }

    fun mainInfo(response: JSONObject) {
        val location = response!!.getJSONObject("location")
        binding.region.text = location.getString("region")
        val current = response.getJSONObject("current")
        binding.temp.text = current.getString("temp_c")
        val condition = current.getJSONObject("condition")
        binding.conText.text = condition.getString("text")
        binding.icon.load("http:" + current.getString("icon"))
    }
}