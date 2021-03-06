package utilities.drivers;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DriverPool {

    public static final int MAX_NUMBER = 5;

    private static final Object waitObj = new Object();
    private static AtomicInteger counter = new AtomicInteger(0);
    private static Logger log = Logger.getLogger(DriverPool.class);

    private static volatile ThreadLocal<WebDriver> instance = new ThreadLocal<WebDriver>() {
        @Override
        protected WebDriver initialValue() {
            WebDriver driver = new FirefoxDriver();
            driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
            return driver;
        }
    };

    public static synchronized WebDriver getDriver() {
        try {
            while (counter.get() > MAX_NUMBER) {
                synchronized (waitObj) {
                    waitObj.wait();
                }
            }
            counter.getAndIncrement();
        } catch (InterruptedException e) {
            log.error(e);
        }

        return instance.get();
    }

    public static synchronized void closeDriver() {
        WebDriver driver = instance.get();

        driver.close();
        driver.quit();

        instance.remove();
        counter.decrementAndGet();
        synchronized (waitObj) {
            waitObj.notifyAll();
        }
    }
}
