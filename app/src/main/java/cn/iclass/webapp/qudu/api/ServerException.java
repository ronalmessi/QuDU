package cn.iclass.webapp.qudu.api;

/**
 * 作者：Administrator on 2016/6/29 12:08
 * 邮箱：763522252@qq.com
 */
public class ServerException extends RuntimeException {

    private int errCode = 0;
    /**
     *
     * @param errCode  错误码
     * @param msg    错误信息
     */
    public ServerException(int errCode, String msg) {
        super(msg);
        this.errCode = errCode;
    }

    public int getErrCode() {
        return errCode;
    }
}
