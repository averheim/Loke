package services.spendperaccount;

import model.Chart;
import services.Service;

import java.util.List;

public class SpendPerAccountDao implements Service {
    @Override
    public List<Chart> getCharts() {
        return null;
    }

    public class Account {
        public String accountId;
        public String userOwner;
        public String productName;
        public String cost;

    }
}
