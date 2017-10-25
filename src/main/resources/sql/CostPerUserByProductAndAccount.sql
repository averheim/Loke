-- Cost / User / Account
SELECT
  user_owner,
  account.name                                              AS account_name,
  product_name,
  sum(cast(blended_cost AS DOUBLE))                         AS cost,
  cast(date_parse(usage_start_date, '%Y-%m-%d %T') AS DATE) AS start_date
FROM wsbillingreports.report_item
INNER JOIN wsbillingreports.account
        ON wsbillingreports.report_item.linked_account_id = wsbillingreports.account.id
WHERE blended_cost != 'BlendedCost'
AND blended_cost != 'blended_cost'
AND usage_start_date != ''
AND usage_start_date != 'UsageStartDate'
AND usage_start_date != 'usage_start_date'
AND user_owner != ''
AND blended_cost != '0.0000000000'
AND cast(date_parse(usage_start_date, '%Y-%m-%d %T') AS DATE) > (current_date - INTERVAL '30' DAY)
GROUP BY user_owner, account.name, product_name, cast(date_parse(usage_start_date, '%Y-%m-%d %T') AS DATE)
ORDER BY user_owner, account.name, start_date ASC;