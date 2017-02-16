package com.vunke.sharehome.rx;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * C_only：Administrator on 2016/1/26 20:25
 */
public final class RxBus {
	private static RxBus instance;

	private final Subject mSubject;

	private RxBus() {
		mSubject = new SerializedSubject(PublishSubject.create());
	}

	public static RxBus getInstance() {
		if (instance == null) {
			synchronized (RxBus.class) {
				if (instance == null) {
					instance = new RxBus();
				}
			}
		}

		return instance;
	}

	public void post(Object o) {
		mSubject.onNext(o);

	}

	public <T extends Object> Observable<T> toObservable(final Class<T> type) {

		return mSubject.filter(new Func1() {
			@Override
			public Object call(Object o) {
				return type.isInstance(o);
			}
		}).cast(type);
	}

}
