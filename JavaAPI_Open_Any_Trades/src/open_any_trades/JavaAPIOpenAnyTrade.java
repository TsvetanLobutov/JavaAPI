package open_any_trades;

import com.fxcm.external.api.transport.FXCMLoginProperties;
import com.fxcm.external.api.transport.GatewayFactory;
import com.fxcm.external.api.transport.IGateway;
import com.fxcm.external.api.transport.listeners.IGenericMessageListener;
import com.fxcm.external.api.transport.listeners.IStatusMessageListener;
import com.fxcm.fix.SubscriptionRequestTypeFactory;
import com.fxcm.fix.pretrade.MarketDataRequest;
import com.fxcm.fix.pretrade.MarketDataSnapshot;
import com.fxcm.fix.pretrade.TradingSessionStatus;
import com.fxcm.messaging.ISessionStatus;
import com.fxcm.messaging.ITransportable;

import com.fxcm.fix.FXCMTimingIntervalFactory;

public class JavaAPIOpenAnyTrade implements IGenericMessageListener, IStatusMessageListener {

    private final IGateway gateway;
    private FXCMLoginProperties loginProperties;
    private TradingSessionStatus tradingSessionStatus;

    private String username;
    private String password;
    private String station;
    private String server;

    public static void main(String[] args) throws Exception {
        JavaAPIOpenAnyTrade japl = new JavaAPIOpenAnyTrade();
        japl.loadConfig();
        japl.login();
        System.in.read();
        japl.logout();
    }

    public JavaAPIOpenAnyTrade() {
        this.gateway = GatewayFactory.createGateway();
    }

    public boolean loadConfig() {
        return loadConfig("D291043975", "6411", "Demo", "http://www.fxcorporate.com/Hosts.jsp");
    }

    public boolean loadConfig(String username, String password, String station, String server) {
        this.username = username;
        this.password = password;
        this.station = station;
        this.server = server;

        this.loginProperties = new FXCMLoginProperties(
                this.username,
                this.password,
                this.station,
                this.server);
        return true;
    }

    public boolean login() throws Exception {
        this.gateway.registerGenericMessageListener(this);
        this.gateway.registerStatusMessageListener(this);
        this.gateway.login(this.loginProperties);
        this.gateway.requestTradingSessionStatus();
        return this.gateway.isConnected();
    }

    public void subscribe() {
        MarketDataRequest mdr = new MarketDataRequest();
        mdr.addRelatedSymbol(this.tradingSessionStatus.getSecurity("EUR/USD"));
        mdr.addRelatedSymbol(this.tradingSessionStatus.getSecurity("USD/JPY"));
        mdr.addRelatedSymbol(this.tradingSessionStatus.getSecurity("GBP/USD"));
        mdr.setSubscriptionRequestType(SubscriptionRequestTypeFactory.SUBSCRIBE);
        mdr.setMDEntryTypeSet(MarketDataRequest.MDENTRYTYPESET_ALL);
        mdr.setFXCMTimingInterval(FXCMTimingIntervalFactory.MIN1);



        try {
            this.gateway.sendMessage(mdr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logout() throws Exception {
        this.gateway.logout();
    }

    @Override
    public void messageArrived(ITransportable it) {
        if (it instanceof MarketDataSnapshot) {
            this.messageArrived((MarketDataSnapshot) it);
        } else if (it instanceof TradingSessionStatus) {
            this.messageArrived((TradingSessionStatus) it);
        }
    }

    public void messageArrived(MarketDataSnapshot mds) {
        System.out.println(mds.toString());
    }

    public void messageArrived(TradingSessionStatus tss) {
        System.out.println("tss");
        this.tradingSessionStatus = tss;
        this.subscribe();
    }

    @Override
    public void messageArrived(ISessionStatus iss) {
        // not required
    }
}
