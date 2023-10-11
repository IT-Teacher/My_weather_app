package uz.itteacher.myweatherapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import coil.load
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import uz.itteacher.myweatherapp.adapter.DayAdapter
import uz.itteacher.myweatherapp.adapter.HourAdapter
import uz.itteacher.myweatherapp.databinding.ActivityMainBinding
import uz.itteacher.myweatherapp.model.Day
import uz.itteacher.myweatherapp.model.Hour
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.roundToInt

const val TAG = "TAG"

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var dayAdapter: DayAdapter
    lateinit var hourAdapter: HourAdapter
    lateinit var obj: JSONObject
    lateinit var hourList: MutableList<Hour>
    lateinit var days: MutableList<Day>

    val url =
        "http://api.weatherapi.com/v1/forecast.json?key=9bcfb053b7d247fda8c53154230810&q=Tashkent&days=7&aqi=yes&alerts=yes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        days = mutableListOf()
        hourList = mutableListOf()
        hourAdapter = HourAdapter(hourList)

        dayAdapter = DayAdapter(days, object : DayAdapter.DayInterface {
            override fun dayOnClick(day: String) {
                hourInfo(obj, day)
                val date = obj.getString("date")
                Log.d(TAG, "dayOnClick: ${date}")
            }

        })


        binding.rvDay.adapter = dayAdapter
        binding.rvHour.adapter = hourAdapter

        val requestQueue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(url, object : Response.Listener<JSONObject> {
            override fun onResponse(response: JSONObject?) {
                mainInfo(response!!)

                val forecast = response.getJSONObject("forecast")

                val forecastday = forecast.getJSONArray("forecastday")
                obj = forecastday.getJSONObject(0)

                for (i in 0 until forecastday.length()) {
                    obj = forecastday.getJSONObject(i)
                    dayInfo(obj)
                    hourInfo(obj)
                }

            }

        }, object : Response.ErrorListener {
            override fun onErrorResponse(error: VolleyError?) {
                Log.d(TAG, "onErrorResponse: $error")
            }

        })
        requestQueue.add(request)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun hourInfo(obj: JSONObject) {
        val hours = obj.getJSONArray("hour")
        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("HH")
        val currenth = formatter.format(time)

        for (i in 0 until hours.length()) {
            val hobj = hours.getJSONObject(i)
            val h = hobj.getString("time").substring(11, 13)
            val time = hobj.getString("time").substring(11)
            val temp_c = hobj.getString("temp_c").toDouble().roundToInt().toString() + "° C"
            val condi = hobj.getJSONObject("condition")
            val uri = "https:" + condi.getString("icon")
            val icon = uri
            var day = hobj.getString("time").substring(0, 9)
            if (hourList.size > 0 && hourList.size < 24) {
                hourList.add(Hour(time, temp_c, icon))
            }
            if (h == currenth && hourList.size < 24) {
                hourList.add(Hour(time, temp_c, icon))
            }
            hourAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun hourInfo(obj: JSONObject, day: String?) {
        val hours = obj.getJSONArray("hour")
        for (i in 0 until hours.length()) {
            val hobj = hours.getJSONObject(i)
            val time = hobj.getString("time").substring(11)
            val temp_c = hobj.getString("temp_c").toDouble().roundToInt().toString() + "° C"
            val condi = hobj.getJSONObject("condition")
            val uri = "https:" + condi.getString("icon")
            val icon = uri
            val d = hobj.getString("time").substring(0, 9)
            if (d == day) {
                hourList.add(Hour(time, temp_c, icon))
                hourAdapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun dayInfo(obj: JSONObject) {
        val date = obj.getString("date")
        val day = obj.getJSONObject("day")
        val maxtemp_c = day.getString("maxtemp_c").toDouble().roundToInt().toString() + "° C"
        val mintemp_c = day.getString("mintemp_c").toDouble().roundToInt().toString() + "° C"
        val condition = day.getJSONObject("condition")
        val con_text = condition.getString("text")
        val day_icon = "https:" + condition.getString("icon")
        days.add(Day(date, maxtemp_c, mintemp_c, con_text, day_icon))

        dayAdapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    private fun mainInfo(response: JSONObject) {
        val location = response!!.getJSONObject("location")
        binding.region.text = location.getString("region")
        val current = response.getJSONObject("current")
        binding.temp.text = current.getString("temp_c").toDouble().roundToInt().toString() + "° C"
        val condition = current.getJSONObject("condition")
        binding.conText.text = condition.getString("text")
        val url = "https:" + condition.getString("icon")
        binding.humidity.text = current.getString("humidity") + "%"
        binding.precipitation.text =
            current.getString("precip_mm").toDouble().roundToInt().toString() + "mm"
        binding.wind.text = current.getString("wind_kph") + "kph"
        binding.icon.load(url)
    }
}