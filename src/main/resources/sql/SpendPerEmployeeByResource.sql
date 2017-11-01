-- Cost / user / product / last 30 days
SELECT
  user_owner,
  product_name,
  sum(cast(blended_cost AS DOUBLE))                         AS cost,
  cast(date_parse(usage_start_date, '%Y-%m-%d %T') AS DATE) AS start_date
FROM wsbillingreports.report_item
WHERE blended_cost != 'BlendedCost'
AND blended_cost != 'blended_cost'
AND blended_cost != '0.0000000000'
AND usage_start_date != ''
AND usage_start_date != 'UsageStartDate'
AND usage_start_date != 'usage_start_date'
AND user_owner != ''
AND cast(date_parse(usage_start_date, '%Y-%m-%d %T') AS DATE) > (current_date - INTERVAL '60' DAY)
GROUP BY user_owner, product_name, cast(date_parse(usage_start_date, '%Y-%m-%d %T') AS DATE)
ORDER BY user_owner, start_date ASC;