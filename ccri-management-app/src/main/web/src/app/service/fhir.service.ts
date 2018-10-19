import {EventEmitter, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment.prod";


export enum Formats {
    JsonFormatted = 'jsonf',
    Json = 'json',
    Xml = 'xml',
    EprView = 'epr'
}

@Injectable({
  providedIn: 'root'
})
export class FhirService {


  //private baseUrl : string = 'https://data.developer-test.nhs.uk/ccri-fhir/STU3';
    private baseUrl : string = undefined;

  private GPCbaseUrl : string = 'https://data.developer-test.nhs.uk/ccri/camel/fhir/gpc';
   // private GPCbaseUrl : string = 'http://127.0.0.1:8187/ccri/camel/fhir/gpc';

    private NRLSbaseUrl : string = 'https://data.developer.nhs.uk/nrls-ri';

    private registerUri: string;

  private format : Formats = Formats.JsonFormatted;

    // public smart: SMARTClient;

  public conformance : fhir.CapabilityStatement;

  conformanceChange : EventEmitter<any> = new EventEmitter();

  rootUrlChange : EventEmitter<any> = new EventEmitter();

    formatChange : EventEmitter<any> = new EventEmitter();

   private rootUrl : string = undefined;


    constructor( private http: HttpClient) {


    /*
    const clientSettings = {
      client_id: 'diabetes',
      // Adding the scopes launch or launch/patient depending upon the SMART on FHIR Launch sequence
      scope: 'user/*.read launch openid profile',
      redirect_uri: 'http://localhost:4200/redirect',
      state: '12312'
    };

    console.log('Fhir Service Construct');
    const oauth2Configuration = {
      client: clientSettings,
      server: "http://127.0.0.1:8183/ccri-fhir/STU3"
    };
    */
    // The authorize method of the SMART on FHIR JS client, will take care of completing the OAuth2.0 Workflow



  }

  public getBaseUrl() :string {
        let retStr = this.baseUrl;

        // this should be resolved by app-config.ts but to stop start up errors

        if (retStr === undefined) {
            if (document.baseURI.includes('localhost')) {
                retStr = 'http://127.0.0.1:8183/ccri-fhir/STU3';
            }
            if (document.baseURI.includes('data.developer-test.nhs.uk')) {
                retStr = 'https://data.developer-test.nhs.uk/ccri-fhir/STU3';
            }
            if (document.baseURI.includes('data.developer.nhs.uk')) {
                retStr = 'https://data.developer.nhs.uk/ccri-fhir/STU3';
            }
        }
        return retStr;
  }

  public setRootUrl(rootUrl :string) {
        this.rootUrl = rootUrl;
        this.baseUrl = rootUrl;
        this.rootUrlChange.emit(rootUrl);
  }

    setGPCNRLSUrl(baseUrl : string) {
        this.GPCbaseUrl = baseUrl +'camel/fhir/gpc';
       // GP Connect only at present
    }

  public getRootUrlChange() {
        return this.rootUrlChange;
  }

  public getConformanceChange() {
    return this.conformanceChange;
  }

  public getFormatChange() {
        return this.formatChange;
    }

    public getFormat() {
        return this.format;
    }

      public getFHIRServerBase() {
        return this.baseUrl;
      }
    public getFHIRGPCServerBase() {
        return this.GPCbaseUrl;
    }

    public getFHIRNRLSServerBase() {
        return this.NRLSbaseUrl;
    }

    public setFHIRServerBase(server : string) {
        this.baseUrl = server;

    }



    getHeaders(contentType : boolean = true ): HttpHeaders {

        let headers = new HttpHeaders(
        );
        if (contentType) {
            headers = headers.append( 'Content-Type',  'application/fhir+json' );
            headers = headers.append('Accept', 'application/fhir+json');
        }
        return headers;
    }


    getEPRHeaders(contentType : boolean = true ): HttpHeaders {

        let headers = this.getHeaders(contentType);

        return headers;
    }

    public setOutputFormat(outputFormat : Formats) {
      this.format = outputFormat;
      this.formatChange.emit(outputFormat);
    }

    public getConformance() {
    //  console.log('called CapabilityStatement');
      this.http.get<any>(this.getBaseUrl()+'/metadata',{ 'headers' : this.getHeaders(true)}).subscribe(capabilityStatement =>
      {
          this.conformance = capabilityStatement;

          this.conformanceChange.emit(capabilityStatement);
      },()=>{
          this.conformance = undefined;
          this.conformanceChange.emit(undefined);
      });
  }

  public get(search : string) : Observable<fhir.Bundle> {

    let url : string = this.getFHIRServerBase() + search;
    let headers = new HttpHeaders(
    );

    if (this.format === 'xml') {
      headers = headers.append( 'Content-Type',  'application/fhir+xml' );
      headers = headers.append('Accept', 'application/fhir+xml');
      return this.http.get(url, { headers, responseType : 'blob' as 'blob'});
    } else {
      return this.http.get<any>(url, {'headers': headers});
    }
  }

    public getNRLS(search : string) : Observable<fhir.Bundle> {

        let url : string = this.getFHIRNRLSServerBase() + search;
        let headers = new HttpHeaders(
        );


         //   headers = headers.append( 'Content-Type',  'application/fhir+json' );
        headers = headers.append('Accept', 'application/fhir+json');
        headers = headers.append('Authorization','Bearer eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJpc3MiOiJodHRwczovL2RlbW9uc3RyYXRvci5jb20iLCJzdWIiOiJodHRwczovL2ZoaXIubmhzLnVrL0lkL3Nkcy1yb2xlLXByb2ZpbGUtaWR8ZmFrZVJvbGVJZCIsImF1ZCI6Imh0dHBzOi8vbnJscy5jb20vZmhpci9kb2N1bWVudHJlZmVyZW5jZSIsImV4cCI6MTUzOTM1Mjk3OCwiaWF0IjoxNTM5MzUyNjc4LCJyZWFzb25fZm9yX3JlcXVlc3QiOiJkaXJlY3RjYXJlIiwic2NvcGUiOiJwYXRpZW50L0RvY3VtZW50UmVmZXJlbmNlLnJlYWQiLCJyZXF1ZXN0aW5nX3N5c3RlbSI6Imh0dHBzOi8vZmhpci5uaHMudWsvSWQvYWNjcmVkaXRlZC1zeXN0ZW18MjAwMDAwMDAwMTE3IiwicmVxdWVzdGluZ19vcmdhbml6YXRpb24iOiJodHRwczovL2ZoaXIubmhzLnVrL0lkL29kcy1vcmdhbml6YXRpb24tY29kZXxBTVMwMSIsInJlcXVlc3RpbmdfdXNlciI6Imh0dHBzOi8vZmhpci5uaHMudWsvSWQvc2RzLXJvbGUtcHJvZmlsZS1pZHxmYWtlUm9sZUlkIn0=.');
        headers = headers.append('fromASID','200000000117');
        headers = headers.append('toASID','999999999999');

        return this.http.get<any>(url, {'headers': headers});

    }

    public postGPC(nhsNumber : string) : Observable<fhir.Bundle> {

        let url : string = this.getFHIRGPCServerBase() +'/Patient/$gpc.getstructuredrecord' ;
        let headers = new HttpHeaders(
        );
        headers = headers.append( 'Content-Type',  'application/fhir+json' );
        headers = headers.append('Accept', 'application/fhir+json');
        let body = '{ "resourceType": "Parameters", "parameter": [ { "name": "patientNHSNumber", "valueIdentifier": { "system": "https://fhir.nhs.uk/Id/nhs-number", "value": "'+nhsNumber+'" } }, { "name": "includeAllergies","part": [{"name": "includeResolvedAllergies","valueBoolean": true}]},{"name": "includeMedication","part": [{"name": "includePrescriptionIssues","valueBoolean": true}]}]}';

        return this.http.post<any>(url, body,{'headers': headers});
    }

  public getResource(search : string) : Observable<any> {

    let url = this.getFHIRServerBase() + search;
    let headers = new HttpHeaders(
    );

    if (this.format === 'xml') {
      headers = headers.append( 'Content-Type',  'application/fhir+xml' );
      headers = headers.append('Accept', 'application/fhir+xml');
      return this.http.get(url, { headers, responseType : 'blob' as 'blob'});
    } else {
      return this.http.get<any>(url, {'headers': this.getHeaders(true)});
    }
  }
  public getResults(url : string) : Observable<fhir.Bundle> {
      console.log('getResults');
      let headers = new HttpHeaders();

      if (this.format === 'xml') {
          headers = headers.append( 'Content-Type',  'application/fhir+xml' );
          headers = headers.append('Accept', 'application/fhir+xml');
          return this.http.get(url, { headers, responseType : 'blob' as 'blob'});
      } else {
          return this.http.get<any>(url, {'headers': this.getHeaders(true)});
      }
  }

    getBinary(id: string): Observable<fhir.Binary> {

        const url = this.getBaseUrl() + '/Binary/'+id;

        return this.http.get<fhir.Binary>(url,{ 'headers' : this.getEPRHeaders(true)});

    }
    getBinaryRaw(id: string,): Observable<any> {

        const url = this.getBaseUrl() + '/Binary/'+id;

        return this.http.get(url,{ 'headers' : this.getEPRHeaders(false) , responseType : 'blob' });

    }

    getCompositionDocumentHTML(id: string): Observable<any> {

        const url =this.getBaseUrl() + '/Binary/'+id;

        let headers = this.getEPRHeaders(false);
        headers = headers.append('Content-Type', 'text/html' );

        return this.http
            .get(url, {  headers , responseType : 'text' as 'text'});
    }

    getCompositionDocumentPDF(id: string): Observable<any> {

        const url = this.getBaseUrl() + '/Binary/'+id;

        let headers = this.getEPRHeaders(false);
        headers = headers.append(
            'Content-Type', 'application/pdf' );

        return this.http
            .get(url, { headers, responseType : 'blob' as 'blob'} );
    }
/*
    getCatClientSecret() {
        // This is a marker for entryPoint.sh to replace
        let secret :string = 'SMART_OAUTH2_CLIENT_SECRET';
        if (secret.indexOf('SECRET') != -1) secret = environment.oauth2.client_secret;
        return secret;
    }
*/
    /*
    getClients() {

        this.authService.setCookie();

        if (this.registerUri === undefined) {
            this.registerUri = localStorage.getItem("registerUri");
        }
        let url = this.registerUri.replace('register','');
        url = url + 'api/clients';
        console.log('url = '+url);

        let bearerToken = 'Basic '+btoa(environment.oauth2.client_id+":"+this.getCatClientSecret());

        let headers = new HttpHeaders({'Authorization': bearerToken });
        headers= headers.append('Content-Type','application/json');
        headers = headers.append('Accept','application/json');


        return this.http.get(url, {'headers' : headers }  );
    }
    */

    /*
    launchSMART(appId : string, contextId : string, patientId : string) :Observable<any> {

        // Calls OAuth2 Server to register launch context for SMART App.

        // https://healthservices.atlassian.net/wiki/spaces/HSPC/pages/119734296/Registering+a+Launch+Context

        let bearerToken = 'Basic '+btoa(environment.oauth2.client_id+":"+this.getCatClientSecret());

        const url = localStorage.getItem("tokenUri").replace('token', '') + 'Launch';
        let payload = JSON.stringify({launch_id: contextId, parameters: []});

        let headers = new HttpHeaders({'Authorization': bearerToken });
        headers= headers.append('Content-Type','application/json');

        console.log(payload);
        return this.http.post<any>(url,"{ launch_id : '"+contextId+"', parameters : { username : 'Get Details From Keycloak', patient : '"+patientId+"' }  }", {'headers': headers});
    }
*/
}
