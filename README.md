# Loke

Loke is a cost report generator for Amazon Web Services. Filling the gaping void in AWS billing API, 
Loke uses Amazon Athena to access Amazons billing reports. It then creates a neat cost summary that spans back 30 days.
It is intended to run as a cronjob to send a weekly cost report to the resource owners via email. But not only that!
Loke also sends an admin report to your list of administrators, making it easy to spot unintended cost fluctuations 
before things gets out of hand.

The user email consists of:
##### Tables
- Cost grouped by resource type over 30 days
- Detailed information of resources started last 30 days
- Grouped resources by the their accounts
##### Charts
- Cost grouped by resource type over 30 days
- Cost for all resources by account

The admin email consists of:
##### Tables
- Cost for all resources by account. One for each user.
##### Charts
- Chart depicting total cost for all accounts. One for each user. Intended for spotting drastic trend shifts.

## Prerequisites

Loke constructs the receivers local-part of the address by appending the Owner-tag's value with the domain name 
declared in the configuration file. At this moment, Loke can only find billing data if the AWS resources are tagged 
with the tag key "Owner". Therefore you need to follow a specific naming convention for your email address 
and resource tags in AWS.

Loke uses Amazon SES as email client. You need to use the same account for your billing bucket as you use for SES.  

### Getting Started

**Overview**
1. Setting up Amazon Athena
2. AWS permissions
3. Account name configuration

**Setting up Amazon Athena**
1. Set up a bucket where you keep your AWS billing CSV logs.
2. Create a staging directory for Athena.
3. Create a DDL script like seen below.
```
CREATE EXTERNAL TABLE IF NOT EXISTS billingreport (
invoice_id string,
payer_account_id string,
linked_account_id string,
record_type string,
record_id string,
product_name string,
rate_id string,
subscription_id string,
pricing_plan_id string,
usage_type string,
operation string,
availability_zone string,
reserved_instance string,
item_description string,
usage_start_date string,
usage_end_date string,
usage_quantity string,
blended_rate string,
blended_cost string,
un_blended_rate string,
un_blended_cost string,
resource_id string,
user_application string,
user_component string,
user_name string,
user_node string,
user_owner string
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES (
  'serialization.format' = ',',
  'quoteChar' = '"',
  'field.delim' = ','
) LOCATION 's3://<BUCKET-NAME>/'
TBLPROPERTIES ('has_encrypted_data'='false');
```
Note that Amazon sometimes change their billing csv's. Revisit if needed.

**AWS permissions**

The following policy grants the minimum permissions for Loke to run.
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:ListBucket",
                "s3:GetObject",
                "s3:PutObject"
            ],
            "Resource": [
                "arn:aws:s3:::<BUCKET-NAME>*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "ses:SendEmail"
            ],
            "Resource": [
                "*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "athena:GetExecutionEngine",
                "athena:GetQueryExecution",
                "athena:StartQueryExecution",
                "athena:GetQueryResults",
                "athena:GetQueryResult"
            ],
            "Resource": [
                "*"
            ]
        }
    ]
}
```

**Account name configuration**

The billing reports can only gives us the account number for your different accounts. Therefore, we give you the option 
to declare the names for all your accounts in a resource file named "accounts.csv" to improve readability. 
This file is placed in the same directory as the jar.

accounts.csv example
```
Account name one,095678345986548
Account name two,232784564987239
```

### Installing

Loke uses maven to build and package in a shaded JAR, which can be run on your local machine.

1. Clone the project
2. Package with Maven using the following command: **mvn package**
3. Create a configuration.yaml file in the same directory as the jar. There is an example configuration file in the templates folder.
4. Create a logging properties file in the same directory as the jar. There is an example properties file in the templates folder.
5. (Optional) Create a accounts.csv file in the same directory as the jar. There is an example properties file in the templates folder.
6. Run the jar with the following command:

```
java -Dlog4j.configurationFile="log4j2.xml" -jar loke-1.0-SNAPSHOT-shaded.jar 
```

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Christopher Olsson** [GitHub](https://github.com/Chris015)
* **Markus Averheim** [GitHub](https://github.com/Averheim)

## Acknowledgments

* [Widespace](www.widespace.com)
* Öster forshed
* Johan Björklund
