package actions;

import java.io.IOException;

import javax.servlet.ServletException;

import constants.AttributeConst;
import constants.ForwardConst;
import services.EmployeeService;


/*
 * 認証に関する処理を行うActionクラス
 */
public class AuthAction extends ActionBase{
    private EmployeeService service;

    /*
     * メソッドの実行
     */
    @Override
    public void process() throws ServletException,IOException{

        service = new EmployeeService();

        invoke();
        service.close();
    }
    /*
     * ログイン画面を表示する
     * @throws ServleretException
     * @throws IOException
     */
    public void showLogin() throws ServletException,IOException{
        //CSRF対策用トークンを設定
        putRequestScope(AttributeConst.TOKEN, getTokenId());

        String flush = getSessionScope(AttributeConst.FLUSH);
        if(flush != null) {
            putRequestScope(AttributeConst.FLUSH ,getSessionScope(AttributeConst.FLUSH));
            removeSessionScope(AttributeConst.FLUSH);
        }
        //ログイン画面を表示
        forward(ForwardConst.FW_LOGIN);
    }

}
