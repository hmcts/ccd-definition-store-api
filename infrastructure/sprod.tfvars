asp_name = "ccd-definition-store-api-sprod"
asp_rg = "ccd-definition-store-api-sprod"
capacity = "2"

elastic_search_enabled = "true"
elastic_search_fail_on_import = "true"
elastic_search_index_shards = 2
elastic_search_index_shards_replicas = 1

replicas_datasource_enabled = "true"
replicas_hosts = "ccd-definition-store-api-postgres-db-sprod-read1.postgres.database.azure.com, ccd-definition-store-api-postgres-db-sprod-read2.postgres.database.azure.com"
