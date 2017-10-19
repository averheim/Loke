SELECT
  user_owner,
  linked_account_id,
  product_name,
  resource_id,
  sum(cast(blended_cost AS DOUBLE)) AS cost
FROM wsbillingreports.report_item
WHERE blended_cost != 'BlendedCost'
  AND usage_start_date != ''
  AND user_owner LIKE '%.%'
  AND cast(date_parse(usage_start_date, '%Y-%m-%d %T') AS DATE) > (current_date - INTERVAL '30' DAY)
GROUP BY user_owner, linked_account_id, product_name, resource_id
ORDER BY user_owner, linked_account_id, product_name, resource_id ASC;