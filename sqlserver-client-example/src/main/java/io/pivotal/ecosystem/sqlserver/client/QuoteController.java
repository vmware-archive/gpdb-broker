package io.pivotal.ecosystem.sqlserver.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/quotes")
class QuoteController {

    @Autowired
    private QuoteRepository quoteRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<Quote> findAllQuotes() {
        List<Quote> q = new ArrayList<>();
        Iterator<Quote> i = quoteRepository.findAll().iterator();
        while (i.hasNext()) {
            q.add(i.next());
        }
        return q;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public Quote saveQuote(@RequestBody Quote quote) {
        return quoteRepository.save(quote);
    }

    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    public void deleteQuote(@RequestBody Quote quote) {
        quoteRepository.delete(quote);
    }

    @RequestMapping("/{symbol}")
    public Quote findBySymbol(@PathVariable String symbol) {
        return quoteRepository.findBySymbol(symbol);
    }

}