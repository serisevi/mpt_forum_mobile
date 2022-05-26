package com.example.forummpt

import okhttp3.*

class OkHttpRequest(client: OkHttpClient) {

    var newClient = client

    fun GET(url: String, callback: Callback): Call {
        val request = Request.Builder()
            .url(url)
            .build()

        val call = newClient.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun POST(url: String, parameters: HashMap<String, String>, callback: Callback): Call {
        val builder = FormBody.Builder()
        val it = parameters.entries.iterator()
        while (it.hasNext()) {
            val pair = it.next() as Map.Entry<*, *>
            builder.add(pair.key.toString(), pair.value.toString())
        }

        val formBody = builder.build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()


        val call = newClient.newCall(request)
        call.enqueue(callback)
        return call
    }

}
