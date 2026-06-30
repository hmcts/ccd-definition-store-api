UPDATE public.field_type
SET regular_expression = '^([A-PR-UWYZ][A-HK-Y0-9][AC-HJKMNPR-VXY0-9]?[ABEHMNPRVWXY0-9]? {1,2}[0-9][ABD-HJLN-UW-Z]{2}|GIR 0AA)$'
WHERE reference = 'Postcode'
AND regular_expression = '^([A-PR-UWYZ0-9][A-HK-Y0-9][AEHMNPRTVXY0-9]?[ABEHMNPRVWXY0-9]? {1,2}[0-9][ABD-HJLN-UW-Z]{2}|GIR 0AA)$';
