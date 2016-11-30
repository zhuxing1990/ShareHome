package com.vunke.sharehome.rx;

import com.vunke.sharehome.utils.WorkLog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class Retrofit_RxJava {

	public Retrofit_RxJava(long callTime, String calledName, String calledPhone, String calledType, String callingPhone,
			String callingType) {

		Retrofit retrofit = new Retrofit.Builder().baseUrl("http://134.175.18.53:8080/")
				.addConverterFactory(GsonConverterFactory.create()).build();

		MissedCallService missedCallService = retrofit.create(MissedCallService.class);
		Call<MissedCallBean> call = missedCallService.getlistData(callTime, calledName, calledPhone, calledType,
				callingPhone, callingType);
		call.enqueue(new Callback<Retrofit_RxJava.MissedCallBean>() {

			@Override
			public void onResponse(Call<MissedCallBean> call, Response<MissedCallBean> response) {
				WorkLog.e("Retrofit_RxJava", "相应成功:" + response);

			}

			@Override
			public void onFailure(Call<MissedCallBean> call, Throwable e) {
				WorkLog.e("Retrofit_RxJava", "请求错误:" + e.getMessage());
			}
		});

	}

	public interface MissedCallService {
		@FormUrlEncoded
		@POST("ShareHome/missedCall.do")
		Call<MissedCallBean> getlistData(@Field("callTime") long callTime, @Field("calledName") String calledName,
				@Field("calledPhone") String calledPhone, @Field("calledType") String calledType,
				@Field("callingPhone") String callingPhone, @Field("callingType") String callingType);
	}

	public class MissedCallBean {
		private int code;
		private String data;
		private String message;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@Override
		public String toString() {
			return "MissedCallBean [code=" + code + ", data=" + data + ", message=" + message + "]";
		}
	}
}
