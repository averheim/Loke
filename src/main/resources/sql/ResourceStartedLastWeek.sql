SELECT
  linked_account_id AS account_id,
  user_owner,
  product_name,
  resource_id,
  MIN(usage_start_date) AS start_date,
  sum(cast(blended_cost AS DOUBLE)) AS cost
FROM wsbillingreports.report_item
WHERE blended_cost != 'BlendedCost'
      AND usage_start_date != ''
      AND user_owner LIKE '%.%'
      AND resource_id NOT IN
          (
            SELECT DISTINCT resource_id, usage_start_date
            FROM wsbillingreports.report_item
            WHERE usage_start_date != ''
                  AND usage_start_date != 'UsageStartDate'
                  AND cast(date_parse(usage_start_date, '%Y-%m-%d %T') AS DATE) BETWEEN (current_date - INTERVAL '60' DAY)
                  AND(current_date - INTERVAL '20' DAY)
          )
GROUP BY linked_account_id, user_owner, product_name, resource_id
ORDER BY user_owner, linked_account_id, product_name, resource_id, start_date ASC;