package com.nusantara.automate;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Sleeper;

public class WebElementWait extends FluentWait<WebElement>  {
    public final static long DEFAULT_SLEEP_TIMEOUT = 500;

    public WebElementWait(WebElement element, long timeOutInSeconds) {
          this(element, 
        		  java.time.Clock.systemDefaultZone(), 
        		  Sleeper.SYSTEM_SLEEPER, 
        		  timeOutInSeconds, 
        		  DEFAULT_SLEEP_TIMEOUT);
    }

    public WebElementWait(WebElement element, long timeOutInSeconds, long sleepInMillis) {
          this(element, 
        		  java.time.Clock.systemDefaultZone(), 
        		  Sleeper.SYSTEM_SLEEPER, 
        		  timeOutInSeconds, 
        		  sleepInMillis);
    }

    @SuppressWarnings("deprecation")
	protected WebElementWait(WebElement element, Clock clock, Sleeper sleeper, long timeOutInSeconds,
            long sleepTimeOut) {
          super(element, clock, sleeper);
          withTimeout(timeOutInSeconds, TimeUnit.SECONDS);
          pollingEvery(sleepTimeOut, TimeUnit.MILLISECONDS);
          ignoring(NotFoundException.class);
    }

}
