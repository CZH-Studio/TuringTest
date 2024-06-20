package com.example.demo.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.Response;

@RestControllerAdvice
@ResponseBody
public class RestExceptionHandler {

    // /**
    //  * 处理自定义异常s
    //  *
    //  * @param e BusinessException
    //  * @return
    //  */
    // @ExceptionHandler(ServiceException.class)
    // public Response<Long> businessException(UsernameDuplicatedException e) {
    //     Response<Long> response = new Response<>();
    //     log.error("error: {}", e.getMessage(), e);
    //     response.setState(-6);
    //     response.setData((long) 0);
    //     return response;
    // }
    public static final int OK = 1;

    /**
     * 1.@ExceptionHandler表示该方法用于处理捕获抛出的异常
     * 2.什么样的异常才会被这个方法处理呢?所以需要ServiceException.class,这样的话只要是抛出ServiceException异常就会被拦截到handleException方法,此时handleException方法就是请求处理方法,返回值就是需要传递给前端的数据
     * 3.被ExceptionHandler修饰后如果项目发生异常,那么异常对象就会被自动传递给此方法的参数列表上,所以形参就需要写Throwable e用来接收异常对象
     */
    @ExceptionHandler(UsernameDuplicatedException.class)
    public Response<Long> handleExceptionUsernameDup(UsernameDuplicatedException e) {
        Response<Long> result = new Response<>();
        result.setUid((long)-6);
        result.setData((long) 0);
        return result;
    }

    @ExceptionHandler(UsernameNotExistException.class)
    public Response<Long> handleExceptionUsernameNotExi(UsernameNotExistException e) {
        Response<Long> result = new Response<>();
        result.setUid((long)-4);
        result.setData((long) 0);
        return result;
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public Response<Long> handleExceptionPasswordNotMat(PasswordNotMatchException e) {
        Response<Long> result = new Response<>();
        result.setUid((long)-5);
        result.setData((long) 0);
        return result;
    }

    // /**
    //  * 处理空指针的异常
    //  *
    //  * @param e NullPointerException
    //  * @return
    //  * @description 空指针异常定义为前端传参错误，返回400
    //  */
    // @ExceptionHandler(value = NullPointerException.class)
    // public R<String> nullPointerException(NullPointerException e) {
    //     log.error("空指针异常 NullPointerException ", e);
    //     return R.error(ReturnCode.RC400.getCode(), ReturnCode.RC400.getMsg());
    // }

    // /**
    //  * 处理404异常
    //  *
    //  * @param e NoHandlerFoundException
    //  * @return
    //  */
    // @ExceptionHandler(NoHandlerFoundException.class)
    // //@ResponseStatus(HttpStatus.NOT_FOUND)
    // public R<String> noHandlerFoundException(HttpServletRequest req, Exception e) {
    //     log.error("404异常 NoHandlerFoundException, method = {}, path = {} ", req.getMethod(), req.getServletPath(), e);
    //     return R.error(ReturnCode.RC404.getCode(), ReturnCode.RC404.getMsg());
    // }

    // /**
    //  * 处理请求方式错误(405)异常
    //  *
    //  * @param e HttpRequestMethodNotSupportedException
    //  * @return
    //  */
    // @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    // public R<String> HttpRequestMethodNotSupportedException(HttpServletRequest req, Exception e) {
    //     log.error("请求方式错误(405)异常 HttpRequestMethodNotSupportedException, method = {}, path = {}", req.getMethod(), req.getServletPath(), e);
    //     return R.error(ReturnCode.RC405.getCode(), ReturnCode.RC405.getMsg());
    // }

    // /**
    //  * 处理其他异常
    //  *
    //  * @param e otherException
    //  * @return
    //  */
    @ExceptionHandler(Exception.class)
    public Response<Void> elseException(Exception e) {
        Response<Void> result = new Response<>();
        result.setUid((long)0);
        System.out.println(e.getMessage());
        return result;
    }
}