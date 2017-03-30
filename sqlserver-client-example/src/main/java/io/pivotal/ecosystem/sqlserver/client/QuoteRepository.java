package io.pivotal.ecosystem.sqlserver.client;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
interface QuoteRepository extends PagingAndSortingRepository<Quote, String> {

    Quote findBySymbol(String symbol);
}