package cn.iclass.webapp.qudu.api;

public abstract class AbstractRequestCallback<T> {

    public abstract void onSuccess(T t);

    public abstract void onFailure(int statusCode, String errorMsg);

}
