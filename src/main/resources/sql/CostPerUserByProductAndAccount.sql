SELECT
  user_owner,
  linked_account_id,
  product_name,
  sum(cast(blended_cost AS DOUBLE))                         AS cost,
  cast(date_parse(usage_start_date, '%Y-%m-%d %T') AS DATE) AS start_date
FROM wsbillingreports.report_item
WHERE blended_cost != 'BlendedCost'
AND usage_start_date != ''
AND usage_start_date != 'UsageStartDate'
AND blended_cost != '0.0000000000'
AND cast(date_parse(usage_start_date, '%Y-%m-%d %T') AS DATE) > (current_date - INTERVAL '30' DAY)
GROUP BY user_owner, linked_account_id, product_name, cast(date_parse(usage_start_date, '%Y-%m-%d %T') AS DATE)
ORDER BY user_owner, linked_account_id, start_date ASC;