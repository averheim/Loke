package db.athena;

import db.ResourceLoader;

/**
 * Service for fetching invoice items from Athena.
 */
public class InvoiceItemDao {
    private static final InvoiceItemDao ourInstance = new InvoiceItemDao();
    public static final String SQL_INVOICE_ITEMS = ResourceLoader.getResource("sql/settle/invoiceitems.sql");
    private InvoiceItemDao() {
    }
    public static InvoiceItemDao getInstance() {
        return ourInstance;
    }

    /*
    public List<InvoiceItem> getLineItems(AthenaClient theClient, Month theMonth) {
        String aSql = SQL_INVOICE_ITEMS;
        // Set the start and end date for period
        aSql = aSql.replace("<START>", theMonth.format("yyyy-MM-dd"));
        aSql = aSql.replace("<END>", theMonth.getNext().format("yyyy-MM-dd"));
        return theClient
                .executeQuery(aSql, InvoiceItem.class)
                .getResultList();
    }

    public static class InvoiceItem extends ToJsonString {
        public String  period;
        public Integer senderAccountId;
        public String  senderPartyId;
        public Integer senderNetsuiteId;
        public Integer senderSubsidiaryId;
        public Integer receiverAccountId;
        public String  receiverPartyId;
        public Integer receiverNetsuiteId;
        public Integer receiverSubsidiaryId;
        public Integer advAccountId;
        public Integer pubAccountId;
        public Integer nwkAccountId;
        public Integer agencyAccountId;
        public Integer custAccountId;
        public Integer campaignId;
        public String  campaignName;
        public String  campaignRef;
        public int     modelId;
        public String  costItemId;
        public String  lineItemId;
        public String  currencyId;
        public double  amount;
        public double  amountInEur;
        public int     charges;
        public String getInvoiceRef() {
            String aSender = senderPartyId + (senderAccountId != null ? senderAccountId.toString() : "");
            String aReceiver = receiverPartyId + (receiverAccountId != null ? receiverAccountId.toString() : "");
            return period + "-" + aSender + "-" + aReceiver + "-" + currencyId;
        }
    }
    */
}
