import { IsArray, IsString } from 'class-validator';

export class SetCaseTagsDto {
  @IsArray()
  @IsString({ each: true })
  tagIds!: string[];
}
