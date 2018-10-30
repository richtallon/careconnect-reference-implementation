import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../../service/fhir.service";
import {EprService} from "../../../service/epr.service";

@Component({
  selector: 'app-patient-vital-signs',
  templateUrl: './patient-vital-signs.component.html',
  styleUrls: ['./patient-vital-signs.component.css']
})
export class PatientVitalSignsComponent implements OnInit {

    observations : fhir.Observation[] = [];

  constructor(private router : Router, private fhirSrv : FhirService,  private route: ActivatedRoute, private eprService : EprService) { }

  ngOnInit() {
      let patientid = this.route.snapshot.paramMap.get('patientid');

      this.fhirSrv.get('/Observation?patient='+patientid).subscribe(
          bundle => {
              if (bundle.entry !== undefined) {
                  for (let entry of bundle.entry) {

                      switch (entry.resource.resourceType) {

                          case 'Observation':
                              let observation: fhir.Observation = <fhir.Observation> entry.resource;
                              this.observations.push(observation);
                              break;
                      }

                  }
              }

          }
      );
  }

}