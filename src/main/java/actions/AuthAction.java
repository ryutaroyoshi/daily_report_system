package actions;

import java.io.IOException;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.MessageConst;
import constants.PropertyConst;
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

    /*
     * ログイン処理を行う
     * @throws ServletException
     * @throws IOException
     */
    public void login() throws ServletException,IOException{

        String code = getRequestParam(AttributeConst.EMP_CODE);
        String plainPass = getRequestParam(AttributeConst.EMP_PASS);
        String pepper = getContextScope(PropertyConst.PEPPER);

        //有効な従業員か認証する
        Boolean isValidEmployee = service.validateLogin(code ,plainPass, pepper);

        if(isValidEmployee) {
            //認証成功の場合

            //CSRF対策 tokenのチェック
            if(checkToken()) {

                //ログインした従業員のDBデータを取得
                EmployeeView ev = service.findOne(code ,plainPass, pepper);
                //セッションにログインした従業員を設定
                putSessionScope(AttributeConst.LOGIN_EMP,ev);
                //セッションにログイン完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_LOGINED.getMessage());
                //トップページへリダイレクト
                redirect(ForwardConst.ACT_TOP, ForwardConst.CMD_INDEX);
            }
        } else {
            //認証失敗の場合

            //CSRF
            putSessionScope(AttributeConst.TOKEN ,getTokenId());
            //認証失敗のエラーメッセージ
            putSessionScope(AttributeConst.LOGIN_ERR, true);
            //入力された従業員コードを設定
            putRequestScope(AttributeConst.EMP_CODE, code);

            forward(ForwardConst.FW_LOGIN);
        }
    }

    /*
     * ログアウト処理
     * @throws ServletException
     * @throws IOException
     */
    public void logout() throws ServletException,IOException{

        //セッションからログイン従業員のパラメータを削除
        removeSessionScope(AttributeConst.LOGIN_EMP);

        //セッションにログアウト時のフラッシュメッセージ
        putSessionScope(AttributeConst.FLUSH, MessageConst.I_LOGOUT.getMessage());

        //ログイン画面へ
        redirect(ForwardConst.ACT_AUTH, ForwardConst.CMD_SHOW_LOGIN);

    }

}
