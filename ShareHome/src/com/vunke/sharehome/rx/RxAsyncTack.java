package com.vunke.sharehome.rx;


import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.Subscriber;

public class RxAsyncTack<T> {
	private Subscription subscribe;

	public RxAsyncTack(final RxAsyncTackCallBack<T> callBack) {
		subscribe = Observable.create(new OnSubscribe<T>() {

			/**
			 * 在这里执行耗时操作 并且加入以下代码 subscriber.onNext(T);
			 * subscriber.onError(Throwable)
			 * 
			 * @param subscriber
			 */
			@Override
			public void call(Subscriber<? super T> subscriber) {
				if (callBack != null) {
					callBack.Call(subscriber);
				}
				subscriber.onCompleted();
			}
		})

		.subscribeOn(Schedulers.io())

		.observeOn(AndroidSchedulers.mainThread())

		.subscribe(new Subscriber<T>() {

			/**
			 * 在OnNext执行完之后 (non-Javadoc)
			 * 
			 * @see rx.Observer#onCompleted()
			 */
			@Override
			public void onCompleted() {
				if (callBack != null) {
					callBack.OnCompleted();
					OnUnsubscribe(subscribe);
				}
			}

			/**
			 * 这里写错误的写法 (non-Javadoc)
			 * 
			 * @see rx.Observer#onError(java.lang.Throwable)
			 */
			@Override
			public void onError(Throwable throwable) {
				if (callBack != null) {
					callBack.OnError(throwable);
					OnUnsubscribe(subscribe);
				}
			}

			/**
			 * 耗时操作完成之后在这里执行主线程操作 (non-Javadoc)
			 * 
			 * @see rx.Observer#onNext(java.lang.Object)
			 * 
			 */
			@Override
			public void onNext(T t) {
				if (callBack != null) {
					callBack.OnNext(t);
				}
			}
		});

	}

	public void OnUnsubscribe(Subscription subscribe) {
		if (subscribe.isUnsubscribed()) {
			subscribe.unsubscribe();
		}
	};

	public interface RxAsyncTackCallBack<T> {
		void Call(Subscriber<? super T> subscriber);

		void OnNext(T t);

		void OnCompleted();

		void OnError(Throwable throwable);
	}
}
