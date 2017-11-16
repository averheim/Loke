# Loke

Loke is a cost report generator for Amazon Web Services. Using Amazon Athena to access Amazons billing reports. 
It is intended to run as a cronjob to send a weekly cost report to the resource owners via email.

Loke produces a user email and an admin email. 

The user email consists of:
##### Tables
- Cost grouped by resource type over 30 days
![alt text](https://github.com/images/fixpicture.png "Logo Title Text 1")
- Detailed information of resources started last 30 days
![alt text](https://github.com/images/fixpicture.png "Logo Title Text 1")
- Grouped resources by the their accounts
![alt text](https://github.com/images/fixpicture.png "Logo Title Text 1")
##### Charts
- Cost grouped by resource type over 30 days
![alt text](https://github.com/images/fixpicture.png "Logo Title Text 1")
- Cost for all resources by account
![alt text](https://github.com/images/fixpicture.png "Logo Title Text 1")

The admin email consists of:
##### Tables
- Cost for all resources by account. One for each user.
##### Charts
- Chart depicting cost by account. One for each user.

## Prerequisites

Loke constructs the receivers local-part of the address by appending the Owner-tag's value with the domain name 
declared in the configuration file. At this moment, Loke can only find billing data if the AWS resources are tagged 
with the tag key "Owner". Therefore you need to follow a specific naming convention for your email address 
and resource tags in AWS.

Loke uses Amazon SES as email client. You need to use the same account for your billing bucket as your SES 

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
Account name tvo,232784564987239
```

### Installing

Loke uses maven to build and package in a shaded JAR, which can be run on your local machine.

1. Clone the project
2. Package with Maven using the following command: **mvn package**
3. Create a configuration.yaml file in the same directory as the jar. There is an example configuration file in the templates folder.
4. Create a logging properties file in the same directory as the jar. There is an example properties file in the templates folder.
5. Run the jar with the following command:

```
java -Dlog4j.configurationFile="log4j2.xml" -jar loke-1.0-SNAPSHOT-shaded.jar 
```












































## Running the tests

Explain how to run the automated tests for this system

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Christopher Olsson** [GitHub](https://github.com/Chris015)
* **Markus Averheim** [GitHub](https://github.com/Averheim)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone who's code was used
* Inspiration
* etc
