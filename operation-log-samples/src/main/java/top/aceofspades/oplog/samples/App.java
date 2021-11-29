package top.aceofspades.oplog.samples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.aceofspades.oplog.boot.EnableOperationLog;

/**
 * @author: duanbt
 * @create: 2021-11-25 15:19
 **/
@EnableOperationLog
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
