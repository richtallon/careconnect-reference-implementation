import {EventEmitter, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";


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


  private baseUrl : string = 'https://data.developer.nhs.uk/ccri-fhir/STU3';

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

  public setRootUrl(rootUrl :string) {
        this.rootUrl = rootUrl;
        this.baseUrl = rootUrl;
        this.rootUrlChange.emit(rootUrl);
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
      this.http.get<any>(this.baseUrl+'/metadata',{ 'headers' : this.getHeaders(true)}).subscribe(capabilityStatement =>
      {
          this.conformance = capabilityStatement;

          this.conformanceChange.emit(capabilityStatement);
      },()=>{
          this.conformance = undefined;
          this.conformanceChange.emit(undefined);
      });
  }

  public get(search : string) : Observable<fhir.Bundle> {
      console.log('get');
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

  public getResource(search : string) : Observable<any> {
        console.log('getResource');
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

        const url = this.baseUrl + '/Binary/'+id;

        return this.http.get<fhir.Binary>(url,{ 'headers' : this.getEPRHeaders(true)});

    }
    getBinaryRaw(id: string,): Observable<any> {

        const url = this.baseUrl + '/Binary/'+id;

        return this.http.get(url,{ 'headers' : this.getEPRHeaders(false) , responseType : 'blob' });

    }

    getCompositionDocumentHTML(id: string): Observable<any> {

        const url = this.baseUrl + '/Binary/'+id;

        let headers = this.getEPRHeaders(false);
        headers = headers.append('Content-Type', 'text/html' );

        return this.http
            .get(url, {  headers , responseType : 'text' as 'text'});
    }

    getCompositionDocumentPDF(id: string): Observable<any> {

        const url = this.baseUrl + '/Binary/'+id;

        let headers = this.getEPRHeaders(false);
        headers = headers.append(
            'Content-Type', 'application/pdf' );

        return this.http
            .get(url, { headers, responseType : 'blob' as 'blob'} );
    }

}
