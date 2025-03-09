import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { OwnerService } from 'src/app/service/owner.service';
import { ToastrService } from 'ngx-toastr';
import { OwnerCreate } from 'src/app/dto/owner';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-owner-create',
  templateUrl: './owner-create.component.html',
  styleUrls: ['./owner-create.component.scss'],
  standalone: true,
    imports: [
      FormsModule,
      AutocompleteComponent,
      CommonModule
    ],
})
export class OwnerCreateComponent {
  owner: OwnerCreate = { firstName: '', lastName: '', email: '', description: '' };
  heading: string = 'Create New Owner';
  submitButtonText: string = 'Create';

  constructor(
    private ownerService: OwnerService,
    private router: Router,
    private notification: ToastrService
  ) {}

  onSubmit(form: NgForm): void {
    if (form.valid) {
      this.ownerService.create(this.owner).subscribe({
        next: (data) => {
          this.notification.success(
            `Owner ${data.firstName} ${data.lastName} created successfully.`
          );
          this.router.navigate(['/owners']);
        },
        error: (error: any) => {
          console.error('Error creating owner', error);
          this.notification.error('Could not create owner.', 'Error');
        },
      });
    }
  }
}
