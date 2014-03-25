package code.com.corybill;

import code.com.corybill.control.dataLoad.IronCondorDataLoad;
import code.com.corybill.control.mongoDao.ExpirationsDao;
import code.com.corybill.control.mongoDao.UpdateDateDao;
import code.com.corybill.helper.IronCondorHelper;
import code.com.corybill.helper.SymbolThreadHelper;
import code.com.corybill.helper.TradingConstants;
import code.com.corybill.spring.MySpringLoader;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Cory
 * Date: 12/27/12
 * Time: 11:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrepareIronCondors extends PrepareBase<PrepareIronCondors> {
    private static Logger log = Logger.getLogger(PrepareIronCondors.class);
    private IronCondorDataLoad dataLoad;

    public IronCondorDataLoad getDataLoad() {
        return dataLoad;
    }
    public void setDataLoad(IronCondorDataLoad dataLoad) {
        this.dataLoad = dataLoad;
    }

    @Override
    public PrepareBase<PrepareIronCondors> getInstance(){
        return (PrepareIronCondors) MySpringLoader.getInstance().getContext().getBean("ironCondors");
    }

    @Override
    public void run(){
        dataLoad.setListOfSymbols(list);
        dataLoad.invoke();
    }

    public static void main(String[] args){
        MySpringLoader.getInstance();
        PrepareIronCondors base = new PrepareIronCondors();
        base.start();
    }

    public void start(){
        long start = Calendar.getInstance().getTimeInMillis();

        try {
            ExecutorService es = Executors.newCachedThreadPool();
            SymbolThreadHelper.prepareStarterThreads(new PrepareIronCondors(), es, TradingConstants.IRON_CONDOR_NUM_THREADS);
            es.shutdown();
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error(e.getStackTrace());
        }

        ExpirationsDao expirationsDao = new ExpirationsDao();
        expirationsDao.saveExpirations(IronCondorHelper.getMap());

        UpdateDateDao.updateIronCondorDate();

        long end = Calendar.getInstance().getTimeInMillis();
        double totalTimeInMinutes = (end - start) / 60000.00;
        log.trace("Complete: " + totalTimeInMinutes);
    }
}
