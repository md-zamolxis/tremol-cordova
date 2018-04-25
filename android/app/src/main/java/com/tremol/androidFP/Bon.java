package com.tremol.androidFP;

import android.app.Activity;
import android.content.res.Resources;

import com.tremol.androidFP.Diags.DUtil;
import com.tremol.androidFP.Diags.DUtil.DRES;
import com.tremol.androidFP.Diags.PDiag;
import com.tremol.kb.KBut;
import com.tremol.kb.KCat;
import com.tremol.zfplibj.ZFPArticle;
import com.tremol.zfplibj.ZFPDepartment;
import com.tremol.zfplibj.ZFPLib;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.EnumSet;

import FPData.FPInfo;
import Interfaces.MyRunnable;

public class Bon
{
    enum STATE_SI
    {
        WAIT_QTY, // start
        WAIT_PLU,
        WAIT_NUMBER,
        WAIT_TOTAL,
        WAIT_DEP,
        RADY_EXEC;

        public static final EnumSet<STATE_SI> WAIT_OPERATOR = EnumSet.of(
                WAIT_DEP, WAIT_QTY, WAIT_PLU, WAIT_TOTAL);
    }

    class SaleInfo
    {
        StringBuilder mSb = new StringBuilder();
        public String name;
        public char taxgrp;
        public double price;
        public float qty;
        public float discount;

        public int artnum;
        public int depnum;

        public boolean DB = false;// 32h
        public boolean DEP = false;// 34h
        EnumSet<STATE_SI> state = EnumSet.of(STATE_SI.WAIT_QTY);

        private String view = "";

        void clearSb()
        {
            mSb.delete(0, mSb.length());
        }

        public String addKB(KBut kb) throws Exception
        {
            if (DEP && (kb == KBut.QTY || kb == KBut.PLU || kb == KBut.D || kb == KBut.TOTAL || kb == KBut.DOT))
                throw new Exception(res.getString(R.string.waiting_dep_num));

            switch (kb)
            {
                case QTY:
                    if (qty != 0)
                        throw new Exception(res.getString(R.string.qty_already_entered));
                    try
                    {
                        qty = Float.parseFloat("" + mSb);
                    }
                    catch (Exception e)
                    {
                        throw new Exception(res.getString(R.string.qty_not_valid));
                    }
                    clearSb();
                    view = qty + " x ";
                    return view;

                case PLU:
                    //1
                    try
                    {
                        artnum = Integer.parseInt("" + mSb);
                    }
                    catch (Exception e)
                    {
                        throw new Exception(res.getString(R.string.art_num_not_valid));
                    }
                    clearSb();
                    //2
                    ZFPArticle a = null;
                    try
                    {
                        a = FPInfo.get().getArticle(fp, artnum);
                    }
                    catch (Exception e)
                    {
                        artnum = 0;
                        curMsg = res.getString(R.string.unable_to_load_art) + " :" + e.getMessage();//не е exception за да изчисти дисплея/ it is not an Exception,it is for clearing the display
                        if (qty != 0)
                        {
                            view = qty + " x ";
                            return view;
                        }
                        else
                        {
                            view = "";
                            return view;
                        }
                    }
                    //3
                    if (qty == 0)
                        qty = 1;//throw new Exception("Няма количество");
                    DB = true;

                    taxgrp = a.getTaxGroup();
                    price = a.getPrice();
                    name = a.getName();
                    state = EnumSet.of(STATE_SI.RADY_EXEC);
                    view = qty + " x " + name;
                    return view;

                case D:
                    if (qty == 0)
                        throw new Exception(res.getString(R.string.qty_not_entered));
                    //очаква цена
                    //Expect price
                    try
                    {
                        price = Float.parseFloat("" + mSb);
                    }
                    catch (Exception e)
                    {
                        throw new Exception(res.getString(R.string.price_not_valid));
                    }
                    DEP = true;
                    clearSb();
                    view = qty + " x " + fd2(price) + "  " + res.getString(R.string.from_dep_num_);            //+ res.getString(R.string.price)
                    return view;

                case DOT:
                case ZERO:
                case ONE:
                case TWO:
                case THREE:
                case FOUR:
                case FIVE:
                case SIX:
                case SEVEN:
                case EIGHT:
                case NINE://
                    if (kb == KBut.DOT)
                    {
                        if (qty != 0 && mSb.length() == 0)
                            throw new Exception(res.getString(R.string.waiting_for_price_or_artnum));
                    }
                    mSb.append(kb.getText());
                    if (DEP)// очаква номер на департамент/ expect department number
                    {
                        try
                        {
                            depnum = Integer.parseInt(mSb.toString());
                        }
                        catch (Exception e)
                        {
                            throw new Exception(res.getString(R.string.dep_num_not_valid));
                        }
                        clearSb();
                        view = qty + " x " + price + "  " + fd2(price) + "  " + res.getString(R.string.from_dep_num_) + depnum;
                        state = EnumSet.of(STATE_SI.RADY_EXEC);
                        return view;
                    }
                    else// допълва брой или цена /complements price or count
                    {
                        if (qty == 0)
                            view = mSb.toString();
                        else
                            view += kb.getText();//view +=mSb.toString();
                        return view;
                    }

                default:
                    throw new Exception("???");
            }
        }
    }

    Activity cxt = null;
    Resources res = null;
    final String NL = "\n";
    ZFPLib fp = null;

    SaleInfo curSI = null;
    boolean opened = false;

    String curView = null;
    String curMsg = null;
    String curToLog = null;
    static String bon_psw = null;

    public Bon(Activity cxt)
    {
        this.cxt = cxt;
        this.res = cxt.getResources();
    }

    public void setFP(ZFPLib fp)
    {
        this.fp = fp;
    }

    public String getCurView()
    {
        return curView;
    }

    public String getCurMsg()
    {
        return curMsg;
    }

    public String getCurToLog()
    {
        return curToLog;
    }

    public void setCurToLog(String s)
    {
        curToLog = s;
    }

    public SaleInfo getCurSI()
    {
        return curSI;
    }


    public void add(ZFPArticle art) throws Exception
    {
        if (curSI == null)    //първо въвеждане на артикул/ first entered of article
        {
            this.curSI = new SaleInfo();
        }
        if (curSI.DEP)
        {
            throw new Exception(res.getString(R.string.waiting_dep_num));
        }
        if (curSI.qty == 0 && curSI.mSb.length() == 0)//
        {
            boolean b1 = add(KBut.ONE);
        }
        if (curSI.qty == 0)//сетва се след натискане на QTY/ this will be set after click QTY
        {
            boolean b2 = add(KBut.QTY);
        }
        curSI.clearSb();//маха евентуални ст-ти за PLU num
        char ca[] = Integer.toString(art.getNumber()).toCharArray();
        for (char c : ca)
            if (!add(KBut.get(String.valueOf(c))))
                return;
        add(KBut.PLU);
    }

    //Not Used
    public void add(String depName) throws Exception
    {
        //ArrayList<ZFPDepartment> l= FPInfo.get().getDeps(fp,false,null);
        //ZFPDepartment d=null;
        //for(ZFPDepartment z: l)
        //{
        //	if(z.getName().)
        //}
    }

    public boolean add(KBut kb) throws Exception // връща дали е обработено натискането // return is the message is processed
    {
        curView = null;
        curMsg = null;
        curToLog = null;
        if (kb == KBut.CL)
        {
            if (DUtil.confirm(cxt, res.getString(R.string.confirmation), res.getString(R.string.cancell_curr_sale), true) == DRES.Ok)
            {
                this.curSI = null;
                curView = "";
            }
            return false;
        }
        else if (!this.opened && kb.getKCat() == KCat.NUMBER)
        {
            if (fp.getStatus().isOpenFiscalBon())
            {
                if (DUtil.confirm(cxt, res.getString(R.string.confirmation), res.getString(R.string.conf_open_rec_completion), true) == DRES.Ok)
                {// float sum = fp.calcIntermediateSum(false, false, false, 0.0f,'0');// if(sum>0) fp.payment(-1f, 0, true);fp.closeFiscalBon();
                    this.fp.closeFiscalBonWithAutoPayment();
                    curMsg = res.getString(R.string.compeeting_receip) + NL;
                }
                return false;
            }
            else
            {
                //bellow row allow to enter operator password on every receipt by popup
                //bon_psw=DUtil.input(this.cxt,"PASSWORD","Enter sales password",true).toString();
                if (bon_psw == null)
                    bon_psw = FPInfo.get().getPass(fp);
                ZfpHelper.openFiscalBon(fp, bon_psw, cxt, res);

                this.opened = true;
                curMsg = res.getString(R.string.opening_rec);
                curToLog = "       * * * * *       " + NL +
                        "      " +  res.getString(R.string.openReceipt);
            }
        }
        else if (kb == KBut.TOTAL)
        {
            if (opened)
            {
                MyRunnable mr = new MyRunnable()
                {
                    public void run(final PDiag pd) throws Exception
                    {
                        pd.setMsgPrc(res.getString(R.string.calc_interm_sum), 33);//Thread.sleep(100);
                        this.f = fp.calcIntermediateSum(false, false, false, 0.0f);
                        pd.setMsgPrc(res.getString(R.string.payment), 66);
                        fp.payment(this.f, 0, false);
                        Thread.sleep(100);
                        pd.setMsgPrc(res.getString(R.string.close_rec), 100);
                        fp.closeFiscalBon();
                        Thread.sleep(10);
                        opened = false;
                        curSI = null;
                    }
                };
                PDiag.perform(cxt, res.getString(R.string.close_rec), "", mr);
                if (mr.ex != null)
                    throw mr.ex;
                else
                {
                    curToLog = res.getString(R.string.total) + padLeft(roundFmt(mr.f), 9).replace(' ', '.') + NL +
                            "     " + res.getString(R.string.receipt) + NL;
                    curView = "";
                    return true;
                }
            }
            else
                throw new Exception(res.getString(R.string.no_open_rec));
        }

        if (curSI == null)
            this.curSI = new SaleInfo();

        curView = curSI.addKB(kb);//   ако има грешка да се започне отначало curSI=null ??? / if there is an error it will became from the beggining curSI = null

        if (curSI.state.contains(STATE_SI.RADY_EXEC))
        {
            curToLog = null;
            try
            {
                ZFPDepartment d = null;
                if (curSI.DEP)//продажба за деп.ном или свободна продажба за деп.0 // sell for department number or free price for dep 0
                {
                    if (curSI.depnum == 0)//св.прод // free price
                        freeSale();
                    else
                    {
                        d = fp.getDepartmentInfo(curSI.depnum);
                        fp.sellDepartment(d.getName(), curSI.depnum, curSI.price, curSI.qty);//, curSI.discount);
                        curSI.name = d.getName();
                        curSI.taxgrp = d.getTaxGroup();
                    }
                    curToLog = curSI.name;
                }

                if (curSI.DB)
                {
                    ZFPArticle a = FPInfo.get().getArticle(fp, curSI.artnum);
                    if (a.getPrice() == 0)
                        throw new Exception(res.getString(R.string.art_not_programmed)); ///!

                    fp.sellDB(false, curSI.artnum, curSI.qty);//, curSI.discount);

                    curToLog = (f5.format(curSI.artnum) + " " + curSI.name);
                }

                curToLog += NL + padLeft((fd3(curSI.qty) + " X " + fd2(curSI.price)), 24) + NL;

                String totalSum = roundFmt(curSI.qty * curSI.price);
                curToLog +=
                        padRight(res.getString(R.string.sum) + curSI.taxgrp, 24 - totalSum.length()).replace(' ', '.') + totalSum;
                curView = "";
            }
            catch (Exception e)
            {
                curView = "";
                curToLog = null;
                // curToLog="";
                throw e;
            }
            finally
            {
                curSI = null;    ///!
            }
        }
        return true;
    }


    private void freeSale() throws Exception
    {
        curSI.name = DUtil.input(this.cxt, res.getString(R.string.input), res.getString(R.string.enter_art_name), true).toString();
        //old way, choosing Tax group by popup
        curSI.taxgrp = DUtil.choose(this.cxt,res.getString(R.string.choose_tax_gr),"", Arrays.asList(res.getStringArray(R.array.tg_arr)),true).charAt(0);
        //curSI.taxgrp = 'B';
        fp.getTaxPercents();
        fp.sellFree(curSI.name, curSI.taxgrp, curSI.price, curSI.qty, 0f);
    }


    DecimalFormat f5 = new DecimalFormat("00000");
    DecimalFormat fd2 = new DecimalFormat("0.00");
    DecimalFormat fd3 = new DecimalFormat("0.000");

    String fd2(double f)
    {
        return fd2.format(f).replace(',', '.');
    }

    String fd3(double f)
    {
        return fd3.format(f).replace(',', '.');
    }

    private String roundFmt(double f)
    {
        // fmt = new DecimalFormat("0.00");return fmt.format(Math.round(f*100.0)/100.0).replace(',', '.');
        //return fd2((float) (Math.round(f*100.0)/100.0));
        return fd2(f);
    }

    public static String padRight(String s, int n)
    {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(String s, int n)
    {
        return String.format("%1$" + n + "s", s);
    }
}