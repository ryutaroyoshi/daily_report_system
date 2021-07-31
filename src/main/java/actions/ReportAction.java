package actions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import actions.views.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import services.ReportService;

/*
 * 日報に関する処理を行うActionクラス
 */

public class ReportAction extends ActionBase{
    private ReportService service;

    /*
     * メソッドを実行
     */
    @Override
    public void process() throws ServletException,IOException{

        service = new ReportService();

        invoke();
        service.close();
    }

    /*
     * 一覧画面を表示
     * @throws ServletException
     * @throws IOException
     */
    public void index() throws ServletException,IOException{
        //指定されたページ数の一覧画面に表示する日報データを取得
        int page = getPage();
        List<ReportView> reports = service.getAllPerPage(page);

        //全日報データの件数を取得
        long reportsCount = service.countAll();

        putRequestScope(AttributeConst.REPORTS , reports);
        putRequestScope(AttributeConst.REP_COUNT , reportsCount);
        putRequestScope(AttributeConst.PAGE , page);
        putRequestScope(AttributeConst.MAX_ROW , JpaConst.ROW_PER_PAGE); //1ページの表示するレコードの数

        //フラッシュメッセージが設定されている場合
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH , flush);
            removeSessionScope(AttributeConst.FLUSH);

        }
        forward(ForwardConst.FW_REP_INDEX);

    }

    /*
     * 新規登録画面の表示
     * @throws ServletException
     * @throws IOException
     */
    public void entryNew() throws ServletException,IOException{

        putRequestScope(AttributeConst.TOKEN, getTokenId());

        //日報情報の空インスタンスに、日報の日付=今日の日付を設定
        ReportView rv = new ReportView();
        rv.setReportDate(LocalDate.now());
        putRequestScope(AttributeConst.REPORT, rv); //日付のみ設定済の日報インスタンス

        //新規登録画面の表示
        forward(ForwardConst.FW_REP_NEW);
    }

    /*
     * 新規登録
     * @throws ServletException
     * @throws IOException
     */
    public void create() throws ServletException,IOException{

        if(checkToken()) {

            //日報の日付情報が入力されていなければ、今日の日付を設定
            LocalDate day = null;
            if(getRequestParam(AttributeConst.REP_DATE) == null
                    || getRequestParam(AttributeConst.REP_DATE).equals("")) {
                day = LocalDate.now();
            } else {
                day = LocalDate.parse(getRequestParam(AttributeConst.REP_DATE));
            }

            //セッションからログイン中の従業員情報を取得
            EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

            //パラメータの値を元に日報情報のインスタンスを作成する
            ReportView rv = new ReportView(
                    null,
                    ev, //ログインしている従業員情報を日報作成者として登録する
                    day,
                    getRequestParam(AttributeConst.REP_TITLE),
                    getRequestParam(AttributeConst.REP_CONTENT),
                    null,
                    null);

            //日報情報登録
            List<String> errors = service.create(rv);

            if(errors.size() > 0) {
                //登録中にエラーがあった場合

                putRequestScope(AttributeConst.TOKEN ,getTokenId());
                putRequestScope(AttributeConst.REPORT, rv); // 入力された日報情報
                putRequestScope(AttributeConst.ERR, errors); //エラーのリスト

                forward(ForwardConst.FW_REP_NEW);
            } else {
                //登録中にエラーがない場合

                //セッションに登録完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_RESISTERED.getMessage());

                redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
            }


        }
    }



}
