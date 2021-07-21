package com.api.mutants.controller;


import com.google.gson.Gson;
import com.api.mutants.controller.dto.ResponseDTO;
import com.api.mutants.controller.dto.SampleDTO;
import com.api.mutants.controller.dto.StatusDTO;
import com.api.mutants.domain.service.MutantService;
import com.api.mutants.kafka.Producer;
import com.api.mutants.domain.dto.Stat;
import com.api.mutants.domain.entity.DnaVerified;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;

import static com.api.mutants.util.Constants.MESSAGE_IS_HUMAN;
import static com.api.mutants.util.Constants.MESSAGE_IS_MUTANT;


@RestController
@RequestMapping("/api")
public class MutantController {

  @Autowired
  private MutantService mutantService;

  @Autowired
  private Producer producer;

  @PostMapping(value = "/mutant" )
  public ResponseEntity<ResponseDTO> detectMutant(@RequestBody @Valid SampleDTO sample) throws Exception {

    boolean result = mutantService.detectMutantDna(sample.getDna());
    String strDna  = Arrays.toString(sample.getDna());

    DnaVerified dnaVerified = DnaVerified.builder()
            .dna( strDna)
            .isMutant(result)
            .id((long) strDna.hashCode())
            .build();

    producer.sendMessage(new Gson().toJson(dnaVerified));

    if(result){
      return new ResponseEntity(ResponseDTO.builder().status(new StatusDTO(HttpStatus.OK.value(),
              MESSAGE_IS_MUTANT)).build(), HttpStatus.OK);
    }else{
      return new ResponseEntity(ResponseDTO.builder().status(new StatusDTO(HttpStatus.FORBIDDEN.value(),
              MESSAGE_IS_HUMAN)).build(), HttpStatus.FORBIDDEN);
    }

  }


  @GetMapping(value = "/stats" )
  public ResponseEntity<Stat> getStats() throws Exception {

    Stat stat = mutantService.getStats();
    return new ResponseEntity(stat, HttpStatus.OK);

  }
}
