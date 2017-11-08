package loke.config;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AccountReaderTest {

    @Test
    public void firstAccount_EqualsToTestAccount() throws Exception {
        Map<String, String> accounts = getAccounts();
        String result = accounts.get("1");
        assertEquals("Test Account", result);
    }

    @Test
    public void secondAccount_EqualsToAccountNumberTwo() throws Exception {
        Map<String, String> accounts = getAccounts();
        String result = accounts.get("2");
        assertEquals("Account number two", result);
    }

    @Test
    public void thirdAccount_returnsNull() throws Exception {
        Map<String, String> accounts = getAccounts();
        String result = accounts.get("3");
        assertEquals(null, result);
    }

    private Map<String, String> getAccounts() {
        String path = ClassLoader.getSystemResource("accounts.csv").getPath();
        Map<String, String> accounts = null;
        try {
            accounts = new AccountReader().readCSV(path);
        } catch (MalformedCSVException e) {
            e.printStackTrace();
        }
        return accounts;
    }
}