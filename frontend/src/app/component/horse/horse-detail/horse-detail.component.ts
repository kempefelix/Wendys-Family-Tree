import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Horse } from 'src/app/dto/horse';
import { HorseService } from 'src/app/service/horse.service';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-horse-detail',
  templateUrl: './horse-detail.component.html',
  styleUrls: ['./horse-detail.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class HorseDetailComponent implements OnInit {
  horse?: Horse;
  horses: Horse[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private horseService: HorseService,
    private notification: ToastrService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.horseService.getById(+id).subscribe({
          next: (horse) => {
            this.horse = horse;
          },
          error: (err) => {
            console.error('Error loading horse details', err);
            this.notification.error('Could not load horse details.', 'Error');
          }
        });
      }
    });
  
    this.horseService.getAll().subscribe({
      next: (horses) => this.horses = horses,
      error: err => console.error('Error loading horses list', err)
    });
  }
  
  getParentName(parentId: number | null): string {
    if (!parentId || !this.horses.length) {
      return 'None';
    }
    const parentHorse = this.horses.find(h => h.id === parentId);
    return parentHorse ? parentHorse.name : 'None';
  }

  editHorse(): void {
    if (this.horse?.id) {
      this.router.navigate(['/horses', this.horse.id, 'edit']);
    }
  }

  deleteHorse(): void {
    if (this.horse?.id) {
      this.horseService.delete(this.horse.id).subscribe({
        next: () => {
          this.notification.success(`${this.horse?.name} deleted successfully.`);
          this.router.navigate(['/horses']);
        },
        error: (err) => {
          console.error('Error deleting horse', err);
          this.notification.error('Could not delete horse.', 'Error');
        }
      });
    }
  }
}
